import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReservationSystem implements Cinema{
    private class ExpirationGuard implements Runnable{
        private Reservation reservation;
        public ExpirationGuard(Reservation reservation){
            this.reservation = reservation;
        }

        @Override
        public void run() {
            this.reservation.isExpired = true;
            seatsAvailable.addAll(this.reservation.seats);
        }
    }

    private class Reservation {
        public Reservation(Set<Integer> seatsToReserve, String user){
            this.seats = seatsToReserve;
            this.user = user;
            scheduler.schedule(new ExpirationGuard(this), timeForConfirmation, TimeUnit.MILLISECONDS);
        }
        public Set<Integer> seats;
        public String user = null;
        public boolean isExpired = false;
    }

    private long timeForConfirmation;
    private ScheduledExecutorService scheduler;
    private Vector<Reservation> reservations = new Vector<Reservation>();
    private Set<Integer> seatsAvailable;

    @Override
    public void configuration(int seats, long timeForConfirmation) throws RemoteException {
        this.timeForConfirmation = timeForConfirmation;
        this.seatsAvailable = new HashSet<Integer>(seats);
        for(int i=0; i < seats; i++){
            this.seatsAvailable.add(i);
        }
    }

    @Override
    public synchronized Set<Integer> notReservedSeats() throws RemoteException {
        return seatsAvailable;
    }

    @Override
    public synchronized boolean reservation(String user, Set<Integer> seats) throws RemoteException {
        for (Integer seat : seats) {
            if(!this.seatsAvailable.contains(seat)){
                return false;
            }
        }
        reservations.add(new Reservation(seats, user));
        seatsAvailable.removeAll(seats);
        return true;
    }

    @Override
    public synchronized boolean confirmation(String user) throws RemoteException {
        for (Reservation reservation : this.reservations) {
            if(reservation.user == user){
                if(!reservation.isExpired){
                    this.reservations.remove(reservation);
                    return true;
                }
                else{
                    for (Integer seat : reservation.seats) {
                        if(!this.seatsAvailable.contains(seat)){
                            this.reservations.remove(reservation);
                            return false;
                        }
                        else{
                            this.seatsAvailable.removeAll(reservation.seats);
                            this.reservations.remove(reservation);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public synchronized String whoHasReservation(int seat) throws RemoteException {
        for (Reservation reservation : this.reservations) {
            if(reservation.seats.contains(seat)){
                return reservation.user;
            }
        }
        return null;
    }
}
