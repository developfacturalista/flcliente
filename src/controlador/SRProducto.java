package controlador;

public class SRProducto {

	private String nombre;
	private String idproductocompuesto;
	private double cantidad;
	private double precio;
	private int nivel;
	private double impuesto;
	private double descuento;
	private String codigo;
	private int productocompuestoprincipal;
	private int multiplicadorcantidad;
	private String plu;
	private String unidadMedida;
	
	
	
	public String getPlu() {
		return plu;
	}
	public void setPlu(String plu) {
		this.plu = plu;
	}
	public String getUnidadMedida() {
		return unidadMedida;
	}
	public void setUnidadMedida(String unidadMedida) {
		this.unidadMedida = unidadMedida;
	}
	public int getProductocompuestoprincipal() {
		return productocompuestoprincipal;
	}
	public void setProductocompuestoprincipal(int productocompuestoprincipal) {
		this.productocompuestoprincipal = productocompuestoprincipal;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getIdproductocompuesto() {
		return idproductocompuesto;
	}
	public void setIdproductocompuesto(String idproductocompuesto) {
		this.idproductocompuesto = idproductocompuesto;
	}
	public double getCantidad() {
		return cantidad;
	}
	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}
	public double getPrecio() {
		return precio;
	}
	public void setPrecio(double precio) {
		this.precio = precio;
	}
	public int getNivel() {
		return nivel;
	}
	public void setNivel(int nivel) {
		this.nivel = nivel;
	}
	public double getImpuesto() {
		return impuesto;
	}
	public void setImpuesto(double impuesto) {
		this.impuesto = impuesto;
	}
	public double getDescuento() {
		return descuento;
	}
	public void setDescuento(double descuento) {
		this.descuento = descuento;
	}
	
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	public SRProducto(String nombre, String idproductocompuesto, double cantidad,
			double precio, int nivel, double impuesto, double descuento,
			String codigo,int productocompuestoprincipal, int multiplicadorcantidad, boolean esNotadeCreditoXNegativo,
			String plu, String unidadMedida) {
		super();
		this.nombre = nombre;
		this.idproductocompuesto = idproductocompuesto;
//		this.cantidad = cantidad;
//		this.precio = precio;
		this.nivel = nivel;
//		this.impuesto = impuesto;
		this.descuento = descuento;
		this.codigo = codigo;
		this.productocompuestoprincipal=productocompuestoprincipal;
		this.multiplicadorcantidad=multiplicadorcantidad;
		this.plu=plu;
		this.unidadMedida=unidadMedida;
		if(esNotadeCreditoXNegativo){
			this.precio = Math.abs(precio);
			this.impuesto = Math.abs(impuesto);
			this.cantidad = Math.abs(cantidad);
		}else{
			if(precio<0){
				this.precio = Math.abs(precio);
				this.impuesto = Math.abs(impuesto);
				this.cantidad = -cantidad;
			}else{
				this.precio = precio;
				this.impuesto = impuesto;
				this.cantidad = cantidad;
			}
		}
		
		
	}
	public int getMultiplicadorcantidad() {
		return multiplicadorcantidad;
	}
	public void setMultiplicadorcantidad(int multiplicadorcantidad) {
		this.multiplicadorcantidad = multiplicadorcantidad;
	}
	
	
}
