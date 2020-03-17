package co.gov.banrep.iconecta.cs.workflow;

public class ValorAtributo {
	
	private Object valor;
	private TipoAtributo tipo;
	private boolean isMultivalor;
	
	
		
	public ValorAtributo(Object valor, TipoAtributo tipo, boolean isMultivalor) {
		super();
		this.valor = valor;
		this.tipo = tipo;
		this.isMultivalor = isMultivalor;
	}
	
	public ValorAtributo(Object valor, TipoAtributo tipo) {
		super();
		this.valor = valor;
		this.tipo = tipo;
		this.isMultivalor = false;
	}
	public Object getValor() {
		return valor;
	}
	public void setValor(Object valor) {
		this.valor = valor;
	}
	public TipoAtributo getTipo() {
		return tipo;
	}
	public void setTipo(TipoAtributo tipo) {
		this.tipo = tipo;
	}
	public boolean isMultivalor() {
		return isMultivalor;
	}
	public void setMultivalor(boolean isMultivalor) {
		this.isMultivalor = isMultivalor;
	}
	
	
	
	

}
