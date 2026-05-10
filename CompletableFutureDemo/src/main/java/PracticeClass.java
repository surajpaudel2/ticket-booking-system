import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PracticeClass {

    public void whatItReturns() {
        System.out.println("Main Thread is executing before running the compatable future.");

        int size = CompletableFuture.supplyAsync(() -> List.of("Alice", "Bob", "Charlie"))
                .thenApply(li -> {
                    li.add("Suraj");
                    li.add("Bibek");
                    return li;
                })
                        .thenApply(li -> li.size());

        System.out.println("Main thread done with executing.");

    }

}
