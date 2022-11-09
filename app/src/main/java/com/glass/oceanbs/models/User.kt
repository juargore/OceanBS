@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.models

data class User (
    var tipoUsuario: Int, //1-Propietario | 2-Colaborador
    var idPropietario: String,
    var idColaborador: String,
    var codigo: String,
    var nombre: String,
    var apellidoP: String,
    var apellidoM: String
)

const val OWNER = 1
const val COLLABORATOR = 2