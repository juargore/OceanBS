@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.models

data class Solicitud(var Id: String,
                     var FechaAlta: String,
                     var HoraAlta: String,
                     var FechaUltimaModif: String,
                     var HoraUltimaModif: String,
                     var Status: String,
                     var Observaciones: String,
                     var IdColaboradorAlta: String,
                     var IdColaboradorUltModif: String,
                     var IdProducto: String,
                     var IdColaborador1: String,
                     var IdColaborador2: String,
                     var IdColaborador3: String,
                     var Codigo: String,
                     var ReportaPropietario: String,
                     var TipoRelacionPropietario: String,
                     var NombrePR: String,
                     var TelCelularPR: String,
                     var TelParticularPR: String,
                     var CorreoElectronicoPR: String,
                     var TipoVigencia: String,
                     var IdDesarrollo: String,
                     var CodigoDesarrollo: String,
                     var NombreDesarrollo: String,
                     var CodigoUnidad: String,
                     var NombreUnidad: String,
                     var IdPropietario: String,
                     var CodigoPropietario: String,
                     var NombrePropietario: String
)