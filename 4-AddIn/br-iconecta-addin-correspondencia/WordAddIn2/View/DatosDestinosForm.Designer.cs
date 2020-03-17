namespace CorrWordAddIn
{
    partial class DatosDestinosForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.dgvDestinos = new System.Windows.Forms.DataGridView();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.dgvCopias = new System.Windows.Forms.DataGridView();
            ((System.ComponentModel.ISupportInitialize)(this.dgvDestinos)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvCopias)).BeginInit();
            this.SuspendLayout();
            // 
            // dgvDestinos
            // 
            this.dgvDestinos.AllowUserToAddRows = false;
            this.dgvDestinos.AllowUserToDeleteRows = false;
            this.dgvDestinos.AllowUserToOrderColumns = true;
            this.dgvDestinos.AllowUserToResizeColumns = false;
            this.dgvDestinos.AllowUserToResizeRows = false;
            this.dgvDestinos.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.DisableResizing;
            this.dgvDestinos.Location = new System.Drawing.Point(12, 25);
            this.dgvDestinos.Name = "dgvDestinos";
            this.dgvDestinos.ReadOnly = true;
            this.dgvDestinos.Size = new System.Drawing.Size(650, 250);
            this.dgvDestinos.TabIndex = 2;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(9, 9);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(168, 13);
            this.label1.TabIndex = 5;
            this.label1.Text = "DESTINATARIOS INGRESADOS";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(12, 289);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(119, 13);
            this.label2.TabIndex = 6;
            this.label2.Text = "COPIAS INGRESADAS";
            // 
            // dgvCopias
            // 
            this.dgvCopias.AllowUserToAddRows = false;
            this.dgvCopias.AllowUserToDeleteRows = false;
            this.dgvCopias.AllowUserToOrderColumns = true;
            this.dgvCopias.AllowUserToResizeColumns = false;
            this.dgvCopias.AllowUserToResizeRows = false;
            this.dgvCopias.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.DisableResizing;
            this.dgvCopias.Location = new System.Drawing.Point(12, 308);
            this.dgvCopias.Name = "dgvCopias";
            this.dgvCopias.ReadOnly = true;
            this.dgvCopias.Size = new System.Drawing.Size(650, 250);
            this.dgvCopias.TabIndex = 7;
            // 
            // DatosDestinosForm
            // 
            this.ClientSize = new System.Drawing.Size(671, 563);
            this.Controls.Add(this.dgvCopias);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.dgvDestinos);
            this.Name = "DatosDestinosForm";
            this.Load += new System.EventHandler(this.DatosDestinosForm_Load_1);
            ((System.ComponentModel.ISupportInitialize)(this.dgvDestinos)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvCopias)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }


        #endregion

        public System.Windows.Forms.DataGridView dgvDestinos;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        public System.Windows.Forms.DataGridView dgvCopias;
    }
}