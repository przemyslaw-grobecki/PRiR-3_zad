import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    public static void main(String[] args) throws AccessException, RemoteException, AlreadyBoundException {
        int PORT = 1099;
        Registry registry = java.rmi.registry.LocateRegistry.createRegistry(PORT);
        
        ReservationSystem reservationSystem = new ReservationSystem();
        Cinema reservationCinema = (Cinema) UnicastRemoteObject.exportObject(reservationSystem, 0);

        registry.rebind( Cinema.SERVICE_NAME, reservationCinema);
        for ( String service : registry.list() ) {
            System.out.println( "Service : " + service );
        }
	}
}
