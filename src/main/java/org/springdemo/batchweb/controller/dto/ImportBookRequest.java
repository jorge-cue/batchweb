package org.springdemo.batchweb.controller.dto;

import java.time.LocalDate;

public record ImportBookRequest(String inputFileName, LocalDate effectiveDate) {
}
