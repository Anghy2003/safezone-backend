package com.ista.springboot.web.app.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.dto.MensajeComunidadCreateDTO;
import com.ista.springboot.web.app.dto.MensajeComunidadDTO;
import com.ista.springboot.web.app.models.entity.MensajeComunidad;
import com.ista.springboot.web.app.models.services.IMensajeComunidadService;

@CrossOrigin(origins = {"http://localhost:4200", "http://10.0.2.2:4200", "*"})
@RestController
@RequestMapping("/api")
public class MensajeComunidadRestController {

    @Autowired
    private IMensajeComunidadService mensajeService;

    @Autowired
    private ChatWebSocketController chatWebSocketController;

    @GetMapping("/mensajes-comunidad/historial")
    public List<MensajeComunidadDTO> historial(
            @RequestParam Long comunidadId,
            @RequestParam(required = false) String canal
    ) {
        if (comunidadId == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "comunidadId es obligatorio"
            );
        }

        List<MensajeComunidad> lista =
                (canal == null || canal.isBlank())
                ? mensajeService.findByComunidad(comunidadId)
                : mensajeService.findByComunidadAndCanal(comunidadId, canal);

        return lista.stream()
                .map(MensajeComunidadDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/mensajes-comunidad/{id}")
    public MensajeComunidadDTO show(@PathVariable Long id) {
        MensajeComunidad m = mensajeService.findById(id);
        if (m == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Mensaje no encontrado"
            );
        }
        return new MensajeComunidadDTO(m);
    }

    @PostMapping("/mensajes-comunidad/enviar")
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeComunidadDTO enviar(@RequestBody MensajeComunidadCreateDTO dto) {

        String canalDefault = "COMUNIDAD";

        MensajeComunidadDTO saved = chatWebSocketController.manejarMensajeChat(dto, canalDefault);

        String canal = (dto.getCanal() != null && !dto.getCanal().isBlank())
                ? dto.getCanal().trim()
                : canalDefault;

        String destino = "VECINOS".equalsIgnoreCase(canal)
                ? "/topic/vecinos-" + saved.getComunidadId()
                : "/topic/comunidad-" + saved.getComunidadId();

        chatWebSocketController.publicar(destino, saved);

        return saved;
    }
}
