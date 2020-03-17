using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Office.Tools.Ribbon;
using System.Windows.Forms;

/// <summary>
/// Clase que contiene las opciónes o pestañas que traera el complemento
/// </summary>
namespace CorrWordAddIn
{
    public partial class BrRibbon
    {

        private void Ribbon1_Load(object sender, RibbonUIEventArgs e)
        {
            Globals.ThisAddIn.eliminarLog();
            Globals.ThisAddIn.leerArchivoConfiguracion();
        }
        string sTipologia;

        private void tbMemorando_Click(object sender, RibbonControlEventArgs e)
        {
            try
            {
                sTipologia = Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadTipologia);
            }
            catch (System.ArgumentException)
            {
                MessageBox.Show("Plantilla incorrecta.");
            }

            sTipologia = Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadTipologia);
            if (sTipologia != "ME")
            {
                MessageBox.Show("Plantilla incorrecta.");
            }

            else if (Globals.ThisAddIn.copiarArchivo() && Globals.ThisAddIn.copiarArchivoPropiedades())
            {
                if (Globals.ThisAddIn.verificarExtencion())
                {
                    if (Globals.ThisAddIn.iContVisitasForms < 2)
                    {
                        SearchForm sForm = new SearchForm();
                        sForm.Visible = true;
                        sForm.verificarAddIn();
                        sForm.verificarPlantilla();
                        sForm.verificarAmbienteTrabajo();
                        Globals.ThisAddIn.iContVisitasForms++;
                    }
                    else
                    {
                        MessageBox.Show("Ya está abierto el complemento.");
                    }
                }
            }
        }


        private void tbCarta_Click(object sender, RibbonControlEventArgs e)
        {
            try
            {
                sTipologia = Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadTipologia);
            }
            catch (System.ArgumentException)
            {
                MessageBox.Show("Plantilla incorrecta.");
            }

            sTipologia = Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadTipologia);

            if (sTipologia != "CA")
            {
                MessageBox.Show("Plantilla incorrecta.");
            }

            else if (Globals.ThisAddIn.copiarArchivo() && Globals.ThisAddIn.copiarArchivoPaises() && Globals.ThisAddIn.copiarArchivoPropiedades())
            {

                if (Globals.ThisAddIn.verificarExtencion())
                {
                    if (Globals.ThisAddIn.iContVisitasForms < 2)
                    {
                        SearchFormCarta sForm = new SearchFormCarta();
                        sForm.Visible = true;
                        sForm.verificarAddIn();
                        sForm.verificarPlantilla();
                        sForm.verificarAmbienteTrabajo();
                        Globals.ThisAddIn.iContVisitasForms++;
                    }
                    else
                    {
                        MessageBox.Show("Ya está abierto el complemento.");
                    }
                }


            }
        }

        private void tbAyuda_Click(object sender, RibbonControlEventArgs e)
        {
            if (Globals.ThisAddIn.copiarArchivoPropiedades())
            {
                Globals.ThisAddIn.abrirUrlManual();
            }
        }
    }
}

