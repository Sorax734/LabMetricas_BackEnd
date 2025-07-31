package com.labMetricas.LabMetricas.Notice.controller;

import com.labMetricas.LabMetricas.Notice.model.dto.NoticeDto;
import com.labMetricas.LabMetricas.Notice.service.NoticeService;
import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.service.UserService;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notices")
@CrossOrigin(origins = "*")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private UserService userService;

    @GetMapping("/my-notices")
    public ResponseEntity<ResponseObject> getMyNotices(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<NoticeDto> notices = noticeService.getActiveNoticesByUser(currentUser);
            
            return ResponseEntity.ok(new ResponseObject(
                "Notificaciones obtenidas exitosamente",
                notices,
                TypeResponse.SUCCESS
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject(
                "Error al obtener notificaciones: " + e.getMessage(),
                TypeResponse.ERROR
            ));
        }
    }

    @GetMapping("/my-all-notices")
    public ResponseEntity<ResponseObject> getAllMyNotices(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<NoticeDto> notices = noticeService.getAllNoticesByUser(currentUser);
            
            return ResponseEntity.ok(new ResponseObject(
                "Todas las notificaciones obtenidas exitosamente",
                notices,
                TypeResponse.SUCCESS
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject(
                "Error al obtener notificaciones: " + e.getMessage(),
                TypeResponse.ERROR
            ));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseObject> getAllNotices() {
        try {
            List<NoticeDto> notices = noticeService.getAllNotices();
            
            return ResponseEntity.ok(new ResponseObject(
                "Todas las notificaciones obtenidas exitosamente",
                notices,
                TypeResponse.SUCCESS
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject(
                "Error al obtener notificaciones: " + e.getMessage(),
                TypeResponse.ERROR
            ));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ResponseObject> getActiveNotices() {
        try {
            List<NoticeDto> notices = noticeService.getAllActiveNotices();
            
            return ResponseEntity.ok(new ResponseObject(
                "Notificaciones activas obtenidas exitosamente",
                notices,
                TypeResponse.SUCCESS
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject(
                "Error al obtener notificaciones activas: " + e.getMessage(),
                TypeResponse.ERROR
            ));
        }
    }



    @GetMapping("/count")
    public ResponseEntity<ResponseObject> getNoticeCount(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<NoticeDto> activeNotices = noticeService.getActiveNoticesByUser(currentUser);
            
            return ResponseEntity.ok(new ResponseObject(
                "Conteo de notificaciones obtenido exitosamente",
                activeNotices.size(),
                TypeResponse.SUCCESS
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject(
                "Error al obtener conteo de notificaciones: " + e.getMessage(),
                TypeResponse.ERROR
            ));
        }
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email);
    }
} 