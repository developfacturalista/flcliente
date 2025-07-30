package controlador;

import servicios.FacturaElectronicaImplServiceStub_old.Documento;
import servicios.FacturaElectronicaImplServiceStub_old.Factura;
import servicios.FacturaElectronicaImplServiceStub_old.Persona;

public class Validador {
	public static boolean rutValido(String rut){
		int[] ponderadores={4,3,2,9,8,7,6,5,4,3,2};
		while(rut.length()<12){
			rut = "0"+rut;
		}
		int cv = Integer.parseInt(rut.charAt(11)+"");
		int suma = 0;
		for(int i=0;i<11;i++){
			suma+=Integer.parseInt(rut.charAt(i)+"")*ponderadores[i];
		}
		int resto = suma % 11;
		int resultado = 11 - resto;
		if(resultado==11)
			resultado = 0;
		if(resultado==10){
			return false;
		}
		
		return resultado==cv;

	}
	public static boolean ciValida(String ci){
		int[] ponderadores={2,9,8,7,6,3,4};
		while(ci.length()<8){
			ci = "0"+ci;
		}
		int cv = Integer.parseInt(ci.charAt(7)+"");
		int suma = 0;
		for(int i=0;i<7;i++){
			suma+=Integer.parseInt(ci.charAt(i)+"")*ponderadores[i];
		}
		int resto = suma % 10;
		int resultado = 10 - resto;
		if(resultado==10)
			resultado = 0;
		return resultado==cv;

	}
	public static String validar(Factura factura){
		if(factura.getReceptor()!=null){
			Persona receptor = factura.getReceptor();
			if(factura.getReceptor().getDocumento()!=null){
				Documento documento = receptor.getDocumento();
				/* 2: RUC (Uruguay) 
				 * 3: C.I. (Uruguay) 
				 * 4: Otros 
				 * 5: Pasaporte (todos los países) 
				 * 6: DNI (documento de identidad de Argentina, Brasil, Chile o Paraguay
				 */
				//Documento - Tipo de documento debe ser RUC para e-factura
				if((factura.getTipo()==111||factura.getTipo()==112||factura.getTipo()==113)
						&&
					(documento.getTipo()!=2)){
					return "Tipo de documento incorrecto.";
				}
				if(documento.getTipo()==2){
					if(!rutValido(documento.getDocumento())){
						return "El RUT no es válido.";
					}
				}
				if(documento.getTipo()==3){
					if(!ciValida(documento.getDocumento())){
						return "La CI no es válida.";
					}
				}
			}
		}
		return null;
	}
}
