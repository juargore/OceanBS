@file:Suppress("SpellCheckingInspection")

package com.ocean.oceanbs.models

data class Solicitud(var Id: String,
                     var Codigo: String,
                     var IdDesarrollo: String,
                     var CodigoDesarrollo: String,
                     var IdProducto: String,
                     var CodigoUnidad: String,
                     var IdPropietario: String,
                     var NombrePropietario: String,
                     var ReportaPropietario: String,
                     var TipoRelacionPropietario: String,
                     var NombrePR: String,
                     var TelCelularPR: String,
                     var TelParticularPR: String,
                     var CorreoElectronicoPR: String,
                     var Observaciones: String,
                     var IdColaborador1: String,
                     var Status: String
)