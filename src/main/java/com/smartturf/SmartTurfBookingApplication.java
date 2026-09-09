package com.smartturf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Smart Turf Booking & Management System.
 * Developed for BSc IT Final-Year Project.
 */
@SpringBootApplication
public class SmartTurfBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTurfBookingApplication.class, args);
        System.out.println("=================================================");
        System.out.println(" Smart Turf Booking System Backend is Running!");
        System.out.println(" REST API Base URL: http://localhost:8080/api");
        System.out.println("=================================================");
    }
}
