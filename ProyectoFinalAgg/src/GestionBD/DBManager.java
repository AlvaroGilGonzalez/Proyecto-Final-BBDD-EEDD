package GestionBD;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.sql.ResultSet;

/**
 * Clase BDManager, en esta clase se incluyen todos los metodos que trabajan de manera directa sobre la base de datos, ya sea haciendo consultas o actualizaciones
 * @author alvaro
 */
public class DBManager {

    // Conexion a la base de datos
    private static Connection conn = null;

    // Configuracion de la conexion a la base de datos
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "tienda";
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "usuario";
    private static final String DB_MSQ_CONN_OK = "CONEXION CORRECTA";
    private static final String DB_MSQ_CONN_NO = "ERROR EN LA CONEXION";

    // Configuracion de la tabla Clientes
    private static final String DB_CLI = "clientes";
    private static final String DB_CLI_SELECT = "SELECT * FROM " + DB_CLI;
    private static final String DB_CLI_ID = "id";
    private static final String DB_CLI_NOM = "nombre";
    private static final String DB_CLI_DIR = "direccion";

    //////////////////////////////////////////////////
    // METODOS DE CONEXION A LA BASE DE DATOS
    //////////////////////////////////////////////////
    ;
    
    /**
     * Intenta cargar el JDBC driver.
     * @return true si pudo cargar el driver, false en caso contrario
     */
    public static boolean loadDriver() {
        try {
            System.out.print("Cargando Driver...");
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.println("OK!");
            return true;
        } catch (ClassNotFoundException ex) {
            System.err.println("No ha sido posible cargar el driver");
            return false;
        } catch (Exception ex) {
        	 System.err.println("No ha sido posible cargar el driver");
            return false;
        }
    }

    /**
     * Intenta conectar con la base de datos.
     *
     * @return true si pudo conectarse, false en caso contrario
     */
    public static boolean connect() {
        try {
            System.out.print("Conectando a la base de datos...");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("OK!");
            return true;
        } catch (SQLException ex) {
            System.err.println("No ha sido posible conectarse a la base de datos");
            return false;
        }
    }

    /**
     * Comprueba la conexionn y muestra su estado por pantalla
     *
     * @return true si la conexionn existe y es valida, false en caso contrario
     */
    public static boolean isConnected() {
        // Comprobamos estado de la conexión
        try {
            if (conn != null && conn.isValid(0)) {
                System.out.println(DB_MSQ_CONN_OK);
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            System.out.println(DB_MSQ_CONN_NO);
            return false;
        }
    }

    /**
     * Cierra la conexion con la base de datos
     */
    public static void close() {
        try {
            System.out.print("Cerrando la conexion...");
            conn.close();
            System.out.println("OK!");
        } catch (SQLException ex) {
            System.err.println("No ha sido posible cerrar la conexion con la base de datos");
        }
    }

    //////////////////////////////////////////////////
    // METODOS DE TABLA CLIENTES
    //////////////////////////////////////////////////
    ;
    
    // Devuelve 
    // Los argumentos indican el tipo de ResultSet deseado
    /**
     * Obtiene toda la tabla clientes de la base de datos
     * @param resultSetType Tipo de ResultSet
     * @param resultSetConcurrency Concurrencia del ResultSet
     * @return ResultSet (del tipo indicado) con la tabla, null en caso de error
     */
    public static ResultSet getTablaClientes(int resultSetType, int resultSetConcurrency) {
        try {
        	PreparedStatement stmt=conn.prepareStatement(DB_CLI_SELECT, resultSetType, resultSetConcurrency);
        	ResultSet rs=stmt.executeQuery();
            //stmt.close();
            return rs;
        } catch (SQLException ex) {
            System.err.println("No ha sido posible obtener la tabla clientes de la base de datos");
            return null;
        }

    }

  
    /**
     * Imprime por pantalla el contenido de la tabla clientes
     */
    public static void printTablaClientes() {
        try {
            ResultSet rs = getTablaClientes(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            while (rs.next()) {
                int id = rs.getInt(DB_CLI_ID);
                String n = rs.getString(DB_CLI_NOM);
                String d = rs.getString(DB_CLI_DIR);
                System.out.println(id + "\t" + n + "\t" + d);
            }
            rs.close();
        } catch (SQLException ex) {
            System.err.println("No ha sido posible imprimir por pantalla la tabla clientes");
        }
    }
    
    /**
     * Imprime los clientes que tengan la direccion introducida por parametro
     * @param direccion (direccion de los clientes que debemos mostrar)
     */
    public static void filtrarClientesDireccion(String direccion) {
  	  try {
			CallableStatement cStmt = conn.prepareCall("{call filtrarDireccion(?)}");
			cStmt.setString(1,direccion);
			cStmt.execute();
			
			ResultSet rs = cStmt.getResultSet();  
			
			while (rs.next()) {  
				 int id = rs.getInt(DB_CLI_ID);
	             String n = rs.getString(DB_CLI_NOM);
	             String d = rs.getString(DB_CLI_DIR);
	             System.out.println(id + "\t" + n + "\t" + d);
          }  
		} catch (SQLException e) {
			System.err.println("No ha sido posible filtrar clientes por su direccion");
		}  
  }

    //////////////////////////////////////////////////
    // METODOS DE UN SOLO CLIENTE
    //////////////////////////////////////////////////
    ;
    
    /**
     * Solicita a la BD el cliente con id indicado
     * @param id id del cliente
     * @return ResultSet con el resultado de la consulta, null en caso de error
     */
    public static ResultSet getCliente(int id) {
        try {
            // Realizamos la consulta SQL
        	String sql = DB_CLI_SELECT + " WHERE " + DB_CLI_ID + "='" + id + "';";
            PreparedStatement stmt = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            //System.out.println(sql);
            ResultSet rs = stmt.executeQuery();
            //stmt.close();
            
            // Si no hay primer registro entonces no existe el cliente
            if (!rs.first()) {
                return null;
            }

            // Todo bien, devolvemos el cliente
            return rs;

        } catch (SQLException ex) {
            System.err.println("No ha sido posible obtener el cliente con el id indicado");
            return null;
        }
    }

    /**
     * Comprueba si en la BD existe el cliente con id indicado
     *
     * @param id id del cliente
     * @return verdadero si existe, false en caso contrario
     */
    public static boolean existsCliente(int id) {
        try {
            // Obtenemos el cliente
            ResultSet rs = getCliente(id);

            // Si rs es null, se ha producido un error
            if (rs == null) {
                return false;
            }

            // Si no existe primer registro
            if (!rs.first()) {
                rs.close();
                return false;
            }

            // Todo bien, existe el cliente
            rs.close();
            return true;

        } catch (SQLException ex) {
            System.err.println("No ha sido posible comprobar si el cliente con el id indicado existe en la base de datos");
            return false;
        }
    }

    /**
     * Imprime los datos del cliente con id indicado
     *
     * @param id id del cliente
     */
    public static void printCliente(int id) {
        try {
            // Obtenemos el cliente
            ResultSet rs = getCliente(id);
            if (rs == null || !rs.first()) {
                System.out.println("Cliente " + id + " NO EXISTE");
                return;
            }
            
            // Imprimimos su información por pantalla
            int cid = rs.getInt(DB_CLI_ID);
            String nombre = rs.getString(DB_CLI_NOM);
            String direccion = rs.getString(DB_CLI_DIR);
            System.out.println("Cliente " + cid + "\t" + nombre + "\t" + direccion);

        } catch (SQLException ex) {
            System.err.println("Error al solicitar cliente " + id);
        }
    }
    
    /**
     * Inserta un nuevo cliente haciendo uso de un procedimiento creado para ello
     * @param nombre (nombre del cliente)
     * @param direccion (direccion del cliente)
     * @return (true si el cliente se ha a�adido, false si no se ha podido a�adir)
     */
    public static boolean nuevoCliente(String nombre,String direccion) {
   	 try {
   		System.out.print("Insertando cliente " + nombre + "...");
			CallableStatement cStmt = conn.prepareCall("{call insertarCliente(?,?,?,?)}");
			cStmt.setString(1, DB_CLI_NOM);
			cStmt.setString(2, DB_CLI_DIR);  
			cStmt.setString(3,nombre);  
			cStmt.setString(4,direccion);  
			
			cStmt.execute(); 
			cStmt.close();
	         System.out.println("OK!");
	         return true;
			
		} catch (SQLException e) {
			System.err.println("No ha sido posible insertar el nuevo cliente haciendo uso del procedimiento");
			return false;
		} 
   }

    /**
     * Solicita a la BD insertar un nuevo registro cliente
     *
     * @param nombre nombre del cliente
     * @param direccion direccion del cliente
     * @return verdadero si pudo insertarlo, false en caso contrario
     */
    public static boolean insertCliente(String nombre, String direccion) {
        try {
            // Obtenemos la tabla clientes
            System.out.print("Insertando cliente " + nombre + "...");
            ResultSet rs = getTablaClientes(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

            // Insertamos el nuevo registro
            rs.moveToInsertRow();
            rs.updateString(DB_CLI_NOM, nombre);
            rs.updateString(DB_CLI_DIR, direccion);
            rs.insertRow();

            // Todo bien, cerramos ResultSet y devolvemos true
            rs.close();
            System.out.println("OK!");
            return true;

        } catch (SQLException ex) {
            System.err.println("No ha sido posible insertar un nuevo cliente en la tabla");
            return false;
        }
    }

    /**
     * Solicita a la BD modificar los datos de un cliente
     *
     * @param id id del cliente a modificar
     * @param nombre nuevo nombre del cliente
     * @param direccion nueva direccion del cliente
     * @return verdadero si pudo modificarlo, false en caso contrario
     */
    public static boolean updateCliente(int id, String nuevoNombre, String nuevaDireccion) {
        try {
            // Obtenemos el cliente
            System.out.print("Actualizando cliente " + id + "... ");
            ResultSet rs = getCliente(id);

            // Si no existe el Resultset
            if (rs == null) {
                System.out.println("Error. ResultSet null.");
                return false;
            }

            // Si tiene un primer registro, lo eliminamos
            if (rs.first()) {
                rs.updateString(DB_CLI_NOM, nuevoNombre);
                rs.updateString(DB_CLI_DIR, nuevaDireccion);
                rs.updateRow();
                rs.close();
                System.out.println("OK!");
                return true;
            } else {
                System.out.println("ERROR. ResultSet vacio.");
                return false;
            }
        } catch (SQLException ex) {
            System.err.println("No ha sido posible modificar los datos del cliente");
            return false;
        }
    }

    /**
     * Solicita a la BD eliminar un cliente
     *
     * @param id id del cliente a eliminar
     * @return verdadero si pudo eliminarlo, false en caso contrario
     */
    public static boolean deleteCliente(int id) {
        try {
            System.out.print("Eliminando cliente " + id + "... ");

            // Obtenemos el cliente
            ResultSet rs = getCliente(id);

            // Si no existe el Resultset
            if (rs == null) {
                System.out.println("ERROR. ResultSet null.");
                return false;
            }

            // Si existe y tiene primer registro, lo eliminamos
            if (rs.first()) {
                rs.deleteRow();
                rs.close();
                System.out.println("OK!");
                return true;
            } else {
                System.out.println("ERROR. ResultSet vacio.");
                return false;
            }

        } catch (SQLException ex) {
            System.err.println("No ha sido posible eliminar el cliente con el id indicado");
            return false;
        }
    }
    
    //////////////////////////////////////////////////
    // METODOS PARA TRABAJAR CON FICHEROS
    //////////////////////////////////////////////////
    
    /**
     * Vuelca todo el contenido de la tabla en un fichero de texto plano
     * @param ruta (ruta donde se encuentra el fichero en el que volcar los datos)
     */
    public static void VolcarDatos(String ruta) {
    	String ruta2="Ficheros/"+ruta;
    	File f=new File(ruta2);
    	
    	try {
			FileWriter escribirFichero=new FileWriter(f);
			
			escribirFichero.write(DB_NAME+"\t"+DB_CLI+"\n");
			escribirFichero.write(DB_CLI_ID+"\t"+DB_CLI_NOM+"\t\t"+DB_CLI_DIR+"\n");
			
			ResultSet rs = getTablaClientes(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			
            while (rs.next()) {
                int id = rs.getInt(DB_CLI_ID);
                String n = rs.getString(DB_CLI_NOM);
                String d = rs.getString(DB_CLI_DIR);
                escribirFichero.write(id+"\t"+n+"\t\t"+d+"\n");
            }
            escribirFichero.close();
            rs.close();
            System.out.println("La informacion se ha volcado correctamente en el archivo.");
			
		} catch (IOException e) {
			System.err.println("No ha sido posible volcar los datos en el fichero con la ruta especificada");
		} catch (SQLException e) {
			System.err.println("No ha sido posible volcar los datos en el fichero con la ruta especificada");
		}
    }
    
    /**
     * Inserta nuevos clientes leyendo la informacion de los mismos desde un fichero
     * @param ruta (ruta donde se encuentra el fichero que debemos leer)
     */
    public static void nuevoClienteFichero(String ruta) {
    	File f=new File(ruta);
    	try {
			Scanner lecturaFichero=new Scanner(f);
			//No utilizamos las dos primeras lineas del fichero ya que en este punto no nos interesan para ninguna operacion
			lecturaFichero.nextLine();
			lecturaFichero.nextLine();
			lecturaFichero.nextLine();
			while(lecturaFichero.hasNext()) {
				String contenidoInsertar=lecturaFichero.nextLine();
				String datosCliente[]=contenidoInsertar.split(",");
				insertCliente(datosCliente[0],datosCliente[1]);
			}
			lecturaFichero.close();
		} catch (FileNotFoundException e) {
			System.err.println("No ha sido posible insertar el cliente leyendo el fichero especificado");
		}
    }
    
    /**
     * Modifica los datos de clientes leyendo la informacion a actualizar en un fichero
     * @param ruta (ruta donde se encuentra el fichero que debemos leer)
     */
    public static void modificarClienteFichero(String ruta) {
    	File f=new File(ruta);
    	
    	try {
			Scanner lecturaFichero=new Scanner(f);
			//No utilizamos las tres primeras lineas del fichero ya que en este punto no nos interesan para ninguna operacion
			lecturaFichero.nextLine();
			lecturaFichero.nextLine();
			lecturaFichero.nextLine();
			while(lecturaFichero.hasNext()) {
				String contenidoActualizar=lecturaFichero.nextLine();
				String datosCliente[]=contenidoActualizar.split(",");
				updateCliente(Integer.parseInt(datosCliente[0]),datosCliente[1],datosCliente[2]);
			}
			lecturaFichero.close();
		} catch (FileNotFoundException e) {
			System.err.println("No ha sido posible modificar el cliente leyendo el fichero especificado");
		}
    	
    }
    
    /**
     * Eliminamos clientes leyendo en un fichero aquellos que se deben eliminar
     * @param ruta (ruta donde se encuentra el fichero que debemos leer)
     */
    public static void eliminarClienteFichero(String ruta) {
    	File f=new File(ruta);
    	
    	try {
			Scanner lecturaFichero=new Scanner(f);
			//No utilizamos las dos primeras lineas del fichero ya que en este punto no nos interesan para ninguna operacion
			lecturaFichero.nextLine();
			lecturaFichero.nextLine();
			while(lecturaFichero.hasNext()) {
				String contenidoActualizar=lecturaFichero.nextLine();
				String datosCliente[]=contenidoActualizar.split(",");
				for(int i=0;i<datosCliente.length;i++) {
					deleteCliente(Integer.parseInt(datosCliente[i]));
				}
			}
			lecturaFichero.close();
		} catch (FileNotFoundException e) {
			System.err.println("No ha sido posible eliminar el cliente leyendo el fichero especificado");
		}
    }
    
	//////////////////////////////////////////////////
	// METODOS PARA CREAR TABLAS
	//////////////////////////////////////////////////
	
	/**
	* Crear una nueva tabla con 3 columnas cuyo nombre es pasado por parametro
	* @param nombre (nombre de la tabla)
	* @param columna1 (nombre de la columna1)
	* @param columna2 (nombre de la columna2)
	* @param columna3 (nombre de la columna3)
	*/
	public static void crearTabla(String nombre,String columna1,String columna2,String columna3) {
		String sentencia="CREATE TABLE "+nombre+"("+columna1+" varchar(100) primary key,"+columna2+" varchar(100),"+columna3+" varchar(100))";
		try {
			PreparedStatement stmt=conn.prepareStatement(sentencia);
			stmt.execute();
			System.out.println("La nueva tabla se ha creado correctamente");
		} catch (SQLException e) {
			System.err.println("No ha sido posible crear una nueva tabla en la base de datos");
		}
	}

}
