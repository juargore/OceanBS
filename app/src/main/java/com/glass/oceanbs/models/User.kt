@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.models

data class User (
    var id: String,
    var colaborador: Boolean,
    var codigo: String,
    var nombre: String,
    var apellidoP: String,
    var apellidoM: String)