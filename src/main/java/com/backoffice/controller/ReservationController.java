package com.backoffice.controller;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.annotations.POST;
import mg.framework.annotations.RequestParam;
import mg.framework.annotations.RestAPI;
import mg.framework.ModelView;

import com.backoffice.dao.HotelDAO;
import com.backoffice.dao.ReservationDAO;
import com.backoffice.model.Reservation;
import com.backoffice.model.Hotel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReservationController {

    private final HotelDAO hotelDAO = new HotelDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();

    /**
     * Affiche le formulaire de réservation
     */
    @GET("reservation/form")
    public ModelView showForm() {
        ModelView mv = new ModelView("reservation-form");
        try {
            List<Hotel> hotels = hotelDAO.findAll();
            mv.addData("hotels", hotels);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement des hôtels : " + e.getMessage());
        }
        return mv;
    }

    /**
     * Enregistre une réservation et retourne au formulaire
     */
    @POST("reservation/save")
    public ModelView save(@RequestParam("clientId") String clientId,
            @RequestParam("nombrePassager") int nombrePassager,
            @RequestParam("dateArrivee") String dateArrivee,
            @RequestParam("hotelId") int hotelId) {
        ModelView mv = new ModelView("reservation-form");
        try {
            Reservation reservation = new Reservation();
            reservation.setClientId(clientId);
            reservation.setNombrePassager(nombrePassager);
            
            String dateTimeStr = dateArrivee.replace("T", " ");
            if (!dateTimeStr.contains(":")) {
                dateTimeStr += " 00:00:00";
            } else if (dateTimeStr.split(":").length == 2) {
                dateTimeStr += ":00";
            }
            LocalDateTime ldt = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            reservation.setDateArrivee(ldt);
            
            reservation.setHotelId(hotelId);

            reservationDAO.insert(reservation);
            mv.addData("success", "Réservation enregistrée avec succès !");

        } catch (Exception e) {
            mv.addData("error", "Erreur lors de l'enregistrement : " + e.getMessage());
        }

        try {
            List<Hotel> hotels = hotelDAO.findAll();
            mv.addData("hotels", hotels);
        } catch (Exception e) {
            // ignore
        }

        return mv;
    }

    /**
     * API REST: Retourne la liste des réservations
     */
    @GET("api/reservation/list")
    @RestAPI
    public Map<String, Object> listJSON() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Reservation> reservations = reservationDAO.findAll();
            response.put("status", "success");
            response.put("data", reservations);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }
}
