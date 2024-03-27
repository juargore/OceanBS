@file:Suppress("SpellCheckingInspection")

package com.ocean.oceanbs.models

data class Incidencia(var Id: String,
                      var FechaAlta: String,
                      var FechaUltimaModif: String,
                      var Observaciones: String,
                      var IdValorClasif1: String,
                      var IdValorClasif2: String,
                      var IdValorClasif3: String,
                      var FallaReportada: String,
                      var FallaReal: String)