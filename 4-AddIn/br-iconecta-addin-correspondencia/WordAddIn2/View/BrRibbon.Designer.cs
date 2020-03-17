namespace CorrWordAddIn
{
    partial class BrRibbon : Microsoft.Office.Tools.Ribbon.RibbonBase
    {
        /// <summary>
        /// Variable del diseñador necesaria.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        public BrRibbon()
            : base(Globals.Factory.GetRibbonFactory())
        {
            InitializeComponent();
        }

        /// <summary> 
        /// Limpiar los recursos que se estén usando.
        /// </summary>
        /// <param name="disposing">true si los recursos administrados se deben desechar; false en caso contrario.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Código generado por el Diseñador de componentes

        /// <summary>
        /// Método necesario para admitir el Diseñador. No se puede modificar
        /// el contenido de este método con el editor de código.
        /// </summary>
        private void InitializeComponent()
        {
            this.tab1 = this.Factory.CreateRibbonTab();
            this.group1 = this.Factory.CreateRibbonGroup();
            this.group2 = this.Factory.CreateRibbonGroup();
            this.group3 = this.Factory.CreateRibbonGroup();
            this.tbMemorando = this.Factory.CreateRibbonToggleButton();
            this.tbCarta = this.Factory.CreateRibbonToggleButton();
            this.tbAyuda = this.Factory.CreateRibbonToggleButton();
            this.tab1.SuspendLayout();
            this.group1.SuspendLayout();
            this.group2.SuspendLayout();
            this.group3.SuspendLayout();
            this.SuspendLayout();
            // 
            // tab1
            // 
            this.tab1.ControlId.ControlIdType = Microsoft.Office.Tools.Ribbon.RibbonControlIdType.Office;
            this.tab1.Groups.Add(this.group1);
            this.tab1.Groups.Add(this.group2);
            this.tab1.Groups.Add(this.group3);
            this.tab1.Label = "ICONECTA";
            this.tab1.Name = "tab1";
            // 
            // group1
            // 
            this.group1.Items.Add(this.tbMemorando);
            this.group1.Name = "group1";
            // 
            // group2
            // 
            this.group2.Items.Add(this.tbCarta);
            this.group2.Name = "group2";
            // 
            // group3
            // 
            this.group3.Items.Add(this.tbAyuda);
            this.group3.Name = "group3";
            // 
            // tbMemorando
            // 
            this.tbMemorando.ControlSize = Microsoft.Office.Core.RibbonControlSize.RibbonControlSizeLarge;
            this.tbMemorando.Image = global::WordAddIn2.Properties.Resources.logo_iconecta;
            this.tbMemorando.Label = "Memorando";
            this.tbMemorando.Name = "tbMemorando";
            this.tbMemorando.ShowImage = true;
            this.tbMemorando.Click += new Microsoft.Office.Tools.Ribbon.RibbonControlEventHandler(this.tbMemorando_Click);
            // 
            // tbCarta
            // 
            this.tbCarta.ControlSize = Microsoft.Office.Core.RibbonControlSize.RibbonControlSizeLarge;
            this.tbCarta.Image = global::WordAddIn2.Properties.Resources.logo_iconecta;
            this.tbCarta.Label = "Carta";
            this.tbCarta.Name = "tbCarta";
            this.tbCarta.ShowImage = true;
            this.tbCarta.Click += new Microsoft.Office.Tools.Ribbon.RibbonControlEventHandler(this.tbCarta_Click);
            // 
            // tbAyuda
            // 
            this.tbAyuda.ControlSize = Microsoft.Office.Core.RibbonControlSize.RibbonControlSizeLarge;
            this.tbAyuda.Image = global::WordAddIn2.Properties.Resources.logo_iconecta;
            this.tbAyuda.Label = "Ayuda";
            this.tbAyuda.Name = "tbAyuda";
            this.tbAyuda.ShowImage = true;
            this.tbAyuda.Click += new Microsoft.Office.Tools.Ribbon.RibbonControlEventHandler(this.tbAyuda_Click);
            // 
            // BrRibbon
            // 
            this.Name = "BrRibbon";
            this.RibbonType = "Microsoft.Word.Document";
            this.Tabs.Add(this.tab1);
            this.Load += new Microsoft.Office.Tools.Ribbon.RibbonUIEventHandler(this.Ribbon1_Load);
            this.tab1.ResumeLayout(false);
            this.tab1.PerformLayout();
            this.group1.ResumeLayout(false);
            this.group1.PerformLayout();
            this.group2.ResumeLayout(false);
            this.group2.PerformLayout();
            this.group3.ResumeLayout(false);
            this.group3.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        internal Microsoft.Office.Tools.Ribbon.RibbonTab tab1;
        internal Microsoft.Office.Tools.Ribbon.RibbonGroup group1;
        internal Microsoft.Office.Tools.Ribbon.RibbonToggleButton tbMemorando;
        internal Microsoft.Office.Tools.Ribbon.RibbonGroup group2;
        internal Microsoft.Office.Tools.Ribbon.RibbonToggleButton tbCarta;
        internal Microsoft.Office.Tools.Ribbon.RibbonGroup group3;
        internal Microsoft.Office.Tools.Ribbon.RibbonToggleButton tbAyuda;
    }

    partial class ThisRibbonCollection
    {
        internal BrRibbon Ribbon1
        {
            get { return this.GetRibbon<BrRibbon>(); }
        }
    }
}
