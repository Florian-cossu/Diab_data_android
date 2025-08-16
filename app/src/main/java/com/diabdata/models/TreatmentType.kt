package com.diabdata.models

enum class TreatmentType(val displayName: String) {
    FAST_ACTING_RAPID_CARTRIDGE("Cartouche Insuline Rapide"),
    FAST_ACTING_RAPID_SYRINGE("Stylo Insuline Rapide"),
    FAST_ACTING_RAPID_VIAL("Flacon Insuline Rapide"),
    SLOW_ACTING_RAPID_SYRINGE("Stylo Insuline Lente"),
    SLOW_ACTING_RAPID_CARTRIDGE("Cartouche Insuline Lente"),
    SLOW_ACTING_RAPID_VIAL("Flacon Insuline Lente"),
    GLUCAGON_SYRINGE("Stylo Glucagen"),
    GLUCAGON_SPRAY("Glucagen Voie Nasale"),
}