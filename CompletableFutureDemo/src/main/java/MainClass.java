public class MainClass {

    public static void main(String[] args) {
        // Create a CompletableFuture that will complete after 2 seconds
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            try {
//                Thread.sleep(2000); // Simulate a long-running task
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return "Hello, World!";
//        })
//                .thenApply(String::toUpperCase);
//
//        // Do something else while the future is running
//        System.out.println("Doing other work...");
//
//        // Wait for the future to complete and get the result
//        String result = future.join(); // This will block until the future is complete
//        System.out.println("Result: " + result);
//
//        System.out.println("All Done");
    }
}
