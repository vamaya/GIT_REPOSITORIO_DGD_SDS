using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CorrWordAddIn
{
    public class DestinoMe : Usuario
    {
        public string pcrNormal { get; set; }
        public string pcrConfidencial { get; set; }

        public DestinoMe(String nombre, String cargo, String dependencia, String pcrNormal, String pcrConfidencial)
        {
            this.nombre = nombre;
            this.cargo = cargo;
            this.dependencia = dependencia;
            this.pcrNormal = pcrNormal;
            this.pcrConfidencial = pcrConfidencial;
        }

        public DestinoMe()
        {

        }
    }
}
