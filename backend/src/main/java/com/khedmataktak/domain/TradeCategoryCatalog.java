package com.khedmataktak.domain;

import java.util.List;

public final class TradeCategoryCatalog {

    private TradeCategoryCatalog() {
    }

    public record TradeCategory(String sector, String code, String labelFr, String labelEn) {
    }

    public static List<TradeCategory> all() {
        return List.of(
                trade("BTP", "plomberie", "Plomberie", "Plumbing"),
                trade("BTP", "menuiserie", "Menuiserie", "Carpentry"),
                trade("BTP", "maconnerie", "Maçonnerie", "Masonry"),
                trade("BTP", "electricite", "Électricité", "Electrical"),
                trade("BTP", "peinture", "Peinture", "Painting"),
                trade("BTP", "carrelage", "Carrelage", "Tiling"),
                trade("BTP", "couverture", "Couverture / toiture", "Roofing"),
                trade("BTP", "chauffage", "Chauffage / climatisation", "HVAC"),
                trade("BTP", "soudure", "Soudure", "Welding"),
                trade("BTP", "metallerie", "Métallerie", "Metalwork"),
                trade("BTP", "terrassement", "Terrassement", "Earthworks"),
                trade("INDUSTRIE", "maintenance", "Maintenance industrielle", "Industrial maintenance"),
                trade("INDUSTRIE", "logistique", "Logistique / manutention", "Logistics"),
                trade("SERVICES", "restauration", "Restauration", "Food service"),
                trade("SERVICES", "nettoyage", "Nettoyage", "Cleaning")
        );
    }

    private static TradeCategory trade(String sector, String code, String labelFr, String labelEn) {
        return new TradeCategory(sector, code, labelFr, labelEn);
    }
}
