package com.example.undoschool.global_learning_platform_backend.service;

import com.example.undoschool.global_learning_platform_backend.exception.BadRequestException;
import java.time.DateTimeException;
import java.time.ZoneId;
import org.springframework.stereotype.Service;

@Service
public class TimeZoneService {

    public ZoneId requireValidZone(String timeZone) {
        try {
            return ZoneId.of(timeZone);
        } catch (DateTimeException | NullPointerException ex) {
            throw new BadRequestException("Invalid IANA time zone: " + timeZone);
        }
    }
}
