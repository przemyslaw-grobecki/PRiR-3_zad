import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

class Client {
    public static void main(String[] args) throws AccessException, RemoteException, AlreadyBoundException {
        try {
            // Init
            Registry registry = LocateRegistry.getRegistry();
            Cinema reservationSystem = (Cinema) registry.lookup(Cinema.SERVICE_NAME);
            Test1(reservationSystem);
            Test2(reservationSystem);
        } catch (Exception e) {

        }
    }

    private static class Customer {
        public Customer(String username, List<Integer> userSeats) {
            this.userSeats = userSeats;
            this.username = username;
        }

        String username;
        List<Integer> userSeats;
    }

    private static void Test2(Cinema reservationSystem) {
        try {
            reservationSystem.configuration(10, 1000);

            String user1 = "1";
            List<Integer> user1Seats = List.of(1, 2);

            String user2 = "2";
            List<Integer> user2Seats = List.of(2, 3);

            String user3 = "3";
            List<Integer> user3Seats = List.of(3, 4);

            String user4 = "4";
            List<Integer> user4Seats = List.of(4, 5);

            String user5 = "5";
            List<Integer> user5Seats = List.of(1, 3, 5);

            List<Customer> userList = new ArrayList<>();
            userList.add(new Customer(user1, user1Seats));
            userList.add(new Customer(user2, user2Seats));
            userList.add(new Customer(user3, user3Seats));
            userList.add(new Customer(user4, user4Seats));
            userList.add(new Customer(user5, user5Seats));

            userList.parallelStream().forEach((customer) -> {
                try {
                    UserCreatesSubscription(customer.username, customer.userSeats, reservationSystem);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void Test1(Cinema reservationSystem) {
        try {
            reservationSystem.configuration(10, 1000);

            // Warmup
            UserChecksForAvailableSeats(reservationSystem);

            // User 1
            String user1 = "1";
            List<Integer> user1Seats = List.of(1, 2, 3, 4, 5);
            UserCreatesSubscription(user1, user1Seats, reservationSystem);
            UserChecksForAvailableSeats(reservationSystem);

            // User 2
            String user2 = "2";
            List<Integer> user2Seats = List.of(2, 3);
            UserCreatesSubscription(user2, user2Seats, reservationSystem);
            UserChecksForAvailableSeats(reservationSystem);

            // User 2 - round 2
            Thread.sleep(1000);
            UserCreatesSubscription(user2, user2Seats, reservationSystem);
            UserChecksForAvailableSeats(reservationSystem);
            UserConfirmsSubscription(user2, reservationSystem);

            // User 1 - confirmation
            UserConfirmsSubscription(user1, reservationSystem);
            UserChecksForAvailableSeats(reservationSystem);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void UserCreatesSubscription(String userName, List<Integer> seats, Cinema reservationSystem) {
        try {
            if (reservationSystem.reservation(userName, new HashSet<>(seats))) {
                System.out.println("Reservation created successfully for user: " + userName);
            } else {
                System.out.println("Reservation could not be created for user: " + userName);
                return;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void UserConfirmsSubscription(String userName, Cinema reservationSystem) {
        try {
            if (reservationSystem.confirmation(userName)) {
                System.out.println("Reservation successfully confirmed for user: " + userName);
            } else {
                System.out.println("Reservation could not be confirmed for user: " + userName);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void UserChecksForAvailableSeats(Cinema reservationSystem) {
        try {
            var freeSeats = reservationSystem.notReservedSeats();
            System.out.println("Currently available seats: ");
            for (Integer integer : freeSeats) {
                System.out.print(integer + " ");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
