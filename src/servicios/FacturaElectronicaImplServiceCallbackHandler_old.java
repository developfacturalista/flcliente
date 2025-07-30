
/**
 * FacturaElectronicaImplServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */

    package servicios;

    /**
     *  FacturaElectronicaImplServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class FacturaElectronicaImplServiceCallbackHandler_old{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public FacturaElectronicaImplServiceCallbackHandler_old(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public FacturaElectronicaImplServiceCallbackHandler_old(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for reimprimirFactura method
            * override this method for handling normal response from reimprimirFactura operation
            */
           public void receiveResultreimprimirFactura(
                    servicios.FacturaElectronicaImplServiceStub_old.ReimprimirFacturaResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from reimprimirFactura operation
           */
            public void receiveErrorreimprimirFactura(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for generarReporteDiario method
            * override this method for handling normal response from generarReporteDiario operation
            */
           public void receiveResultgenerarReporteDiario(
                    servicios.FacturaElectronicaImplServiceStub_old.GenerarReporteDiarioResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from generarReporteDiario operation
           */
            public void receiveErrorgenerarReporteDiario(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for cargarConfiguracion method
            * override this method for handling normal response from cargarConfiguracion operation
            */
           public void receiveResultcargarConfiguracion(
                    servicios.FacturaElectronicaImplServiceStub_old.CargarConfiguracionResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from cargarConfiguracion operation
           */
            public void receiveErrorcargarConfiguracion(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for generarFactura method
            * override this method for handling normal response from generarFactura operation
            */
           public void receiveResultgenerarFactura(
                    servicios.FacturaElectronicaImplServiceStub_old.GenerarFacturaResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from generarFactura operation
           */
            public void receiveErrorgenerarFactura(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getImprimible method
            * override this method for handling normal response from getImprimible operation
            */
           public void receiveResultgetImprimible(
                    servicios.FacturaElectronicaImplServiceStub_old.GetImprimibleResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getImprimible operation
           */
            public void receiveErrorgetImprimible(java.lang.Exception e) {
            }
                


    }
    