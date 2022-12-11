import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ReservationSystem implements Cinema {
    private class ExpirationGuard implements Runnable {
        private Reservation reservation;

        public ExpirationGuard(Reservation reservation) {
            this.reservation = reservation;
        }

        @Override
        public synchronized void run() {
            if(!this.reservation.isConfirmed){
                this.reservation.isExpired = true;
                seatsAvailable.addAll(this.reservation.seats);
            }
        }
    }

    private class Reservation {
        public Reservation(Set<Integer> seatsToReserve, String user) {
            this.seats = seatsToReserve;
            this.user = user;
            scheduler.schedule(new ExpirationGuard(this), timeForConfirmation, TimeUnit.MILLISECONDS);
        }

        public Set<Integer> seats;
        public String user;
        public boolean isExpired = false;
        public boolean isConfirmed = false;
    }

    private long timeForConfirmation;
    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(8);
    private Vector<Reservation> reservations = new Vector<Reservation>();
    private Set<Integer> seatsAvailable;

    public ReservationSystem(){
        try {
            Cinema stub =  (Cinema) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(Cinema.SERVICE_NAME, stub);
        } catch (RemoteException e) {
            System.err.println("RMI-related reservation system exception:");
            e.printStackTrace();
        }
    }

    @Override
    public void configuration(int seats, long timeForConfirmation) {
        this.timeForConfirmation = timeForConfirmation;
        this.seatsAvailable = new HashSet<Integer>(seats);
        this.reservations = new Vector<Reservation>();

        for (int i = 0; i < seats; i++) {
            this.seatsAvailable.add(i);
        }
    }

    @Override
    public synchronized Set<Integer> notReservedSeats() {
        return this.seatsAvailable;
    }

    @Override
    public synchronized boolean reservation(String user, Set<Integer> seats) {
        for (Integer seat : seats) {
            if (!this.seatsAvailable.contains(seat)) {
                return false;
            }
        }
        reservations.add(new Reservation(seats, user));
        seatsAvailable.removeAll(seats);
        return true;
    }

    @Override
    public synchronized boolean confirmation(String user) {
        for (Reservation reservation : this.reservations) {
            if (reservation.user.equals(user)) {
                if (!reservation.isExpired) {
                    this.seatsAvailable.removeAll(reservation.seats);
                    reservation.isConfirmed = true;
                    return true;
                } else {
                    for (Integer seat : reservation.seats) {
                        if (!this.seatsAvailable.contains(seat)) {
                            this.reservations.remove(reservation);
                            return false;
                        }
                    }
                    
                    reservation.isConfirmed = true;
                    this.seatsAvailable.removeAll(reservation.seats);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public synchronized String whoHasReservation(int seat) {
        for (Reservation reservation : this.reservations) {
            if (reservation.seats.contains(seat) && reservation.isConfirmed) {
                return reservation.user;
            }
        }
        return null;
    }
}
