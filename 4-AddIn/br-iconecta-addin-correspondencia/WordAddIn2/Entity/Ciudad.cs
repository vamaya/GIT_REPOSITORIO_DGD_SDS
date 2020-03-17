namespace CorrWordAddIn
{
    /// <summary>
/// Clase con inf de las Ciudades
/// </summary>
    public class Ciudad
    {
        public string idCiudad { get; set; }
        public string idPais { get; set; }
        public string nombreCiudad { get; set; }

        public Ciudad(string idCiudad, string idPais, string nombreCiudad)
        {
            this.idCiudad = idCiudad;
            this.idPais = idPais;
            this.nombreCiudad = nombreCiudad;
        }
    }
}