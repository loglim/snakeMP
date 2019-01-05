package cz.loglim.smp.client.controllers;

import cz.loglim.smp.client.Main;
import cz.loglim.smp.client.net.ServerConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.util.Random;

public class LobbyController {

    @FXML
    Button startButton, randomizeNameButton;
    @FXML
    TextField nicknameTextField;
    @FXML
    Label hintLabel;

    // Constants
    private static final int MINIMAL_NICKNAME_LENGTH = 3;

    // Private
    private static final String[] RANDOM_NAMES = {"Aaron", "Abel", "Abner", "Abraham", "Adam", "Adelbert", "Adrian", "Alan", "Albert", "Alexander", "Alfred", "Algernon", "Alister", "Alonso", "Alphonso", "Alva", "Alvin", "Ambrose", "Amos", "Andrew", "Angus", "Anselm", "Anthony", "Archibald", "Arnold", "Arthur", "Augustus", "Augustine", "Austin", "Avery", "Baldwin", "Barrett", "Bartholomew", "Basil", "Benedict", "Benjamin", "Bennet", "Bernard", "Bert", "Berthold", "Bertram", "Bill", "Blair", "Blake", "Boris", "Bradford", "Bradley", "Brady", "Brandon", "Brenton", "Bret", "Brian", "Broderick", "Bruce", "Bruno", "Burton", "Byron", "Caleb", "Calvin", "Cameron", "Carey", "Carl", "Carol", "Casey", "Caspar", "Cassius", "Cecil", "Cedric", "Charles", "Chester", "Christian", "Christopher", "Clarence", "Clare", "Clark", "Claude", "Clayton", "Clement", "Clifford", "Clinton", "Clive", "Clyde", "Cody", "Colin", "Conrad", "Corey", "Cornelius", "Craig", "Curtis", "Cyril", "Cyrus", "Dale", "Daniel", "Darrell", "David", "Dean", "Delbert", "Dennis", "Derek", "Desmond", "Dexter", "Dominic", "Don", "Donald", "Donovan", "Dorian", "Douglas", "Doyle", "Drew", "Duane", "Dudley", "Duke", "Duncan", "Dustin", "Dwight", "Dylan", "Earl", "Edgar", "Edmund", "Edward", "Edwin", "Egbert", "Elbert", "Eldred", "Elijah", "Elliot", "Ellis", "Elmer", "Elton", "Elvin", "Elvis", "Elwood", "Emery", "Emil", "Emmanuel", "Emmet", "Eric", "Ernest", "Errol", "Ervin", "Ethan", "Eugene", "Eustace", "Evan", "Everard", "Everett", "Fabian", "Felix", "Ferdinand", "Fergus", "Floyd", "Ford", "Francis", "Franklin", "Frederick", "Fred", "Gabriel", "Garrett", "Geoffrey", "George", "Gerald", "Gilbert", "Glenn", "Gordon", "Graham", "Grant", "Gregory", "Griffith", "Guy", "Harold", "Harris", "Harvey", "Hector", "Henry", "Herbert", "Herman", "Hilary", "Homer", "Horace", "Howard", "Hubert", "Hugh", "Humphrey", "Ian", "Ignatius", "Immanuel", "Irvin", "Isaac", "Isidore", "Ivor", "Jack", "Jacob", "James", "Jared", "Jarvis", "Jason", "Jasper", "Jefferson", "Jeffrey", "Jeremy", "Jerome", "Jesse", "Joel", "John", "Jonathan", "Joseph", "Joshua", "Judson", "Julian", "Justin", "Karl", "Keith", "Kelly", "Kelvin", "Kendall", "Kendrick", "Kenneth", "Kent", "Kevin", "Kirk", "Kristopher", "Kurt", "Kyle", "Lambert", "Lamont", "Lancelot", "Laurence", "Lee", "Leo", "Leonard", "Leopold", "Leroy", "Leslie", "Lester", "Lewis", "Lincoln", "Lindon", "Lindsay", "Linus", "Lionel", "Llewellyn", "Lloyd", "Logan", "Lonnie", "Louis", "Lowell", "Lucian", "Luke", "Luther", "Lyle", "Lynn", "Malcolm", "Manuel", "Marion", "Mark", "Marshall", "Martin", "Marvin", "Matthew", "Matthias", "Maurice", "Maximilian", "Maxwell", "Maynard", "Melvin", "Merlin", "Merrill", "Michael", "Miles", "Milo", "Milton", "Mitchell", "Monroe", "Montague", "Montgomery", "Morgan", "Mortimer", "Morton", "Moses", "Murray", "Nathan", "Neal", "Nelson", "Nevill", "Newton", "Nicholas", "Nigel", "Noah", "Noel", "Norbert", "Norris", "Norman", "Norton", "Oliver", "Orson", "Orville", "Osbert", "Osborn", "Oscar", "Osmond", "Oswald", "Otis", "Owen", "Patrick", "Paul", "Percival", "Perry", "Peter", "Philip", "Preston", "Quentin", "Quincy", "Ralph", "Randall", "Randolph", "Raphael", "Raymond", "Reginald", "Rene", "Reuben", "Reynold", "Richard", "Rick", "Robert", "Roderic", "Rodney", "Roger", "Roland", "Rolph", "Roman", "Ronald", "Ron", "Roscoe", "Ross", "Roy", "Rudolph", "Rufus", "Rupert", "Russell", "Ryan", "Samson", "Samuel", "Sanford", "Saul", "Scott", "Sean", "Sebastian", "Serge", "Seth", "Seymour", "Shannon", "Sheldon", "Shelley", "Sherman", "Shelton", "Sidney", "Silas", "Silvester", "Simeon", "Simon", "Solomon", "Sonny", "Spencer", "Stacy", "Stanley", "Stephen", "Stuart", "Terence", "Thaddeus", "Theodore", "Thomas", "Timothy", "Tobias", "Todd", "Tony", "Tracy", "Travis", "Trenton", "Trevor", "Tristram", "Troy", "Tyler", "Tyrone", "Ulysses", "Uriah", "Valentine", "Valerian", "Van", "Vance", "Vaughan", "Vernon", "Victor", "Vincent", "Virgil", "Wallace", "Waldo", "Walter", "Warren", "Wayne", "Wesley", "Wendell", "Wilbert", "Wilbur", "Wiley", "Wilfred", "Willard", "William", "Willis", "Wilson", "Winfred", "Winston", "Woodrow", "Xavier", "Zachary"};

    @FXML
    public void initialize() {
        // Monitor nickname changes
        nicknameTextField.textProperty().addListener((observable, oldValue, newValue) -> checkGameStart());

        String nickname = ServerConnection.getPlayerNickname();
        if (nickname != null) {
            // Restore previously used nickname
            nicknameTextField.setText(nickname);
        } else {
            setRandomName();
        }
    }

    private void checkGameStart() {
        boolean disable = nicknameTextField.getText().length() < MINIMAL_NICKNAME_LENGTH;

        if (disable) {
            hintLabel.setText("(Your nickname must be at least 3 characters!)");
            hintLabel.setTextFill(Color.ORANGERED);
        } else {
            hintLabel.setText("");
        }

        startButton.setDisable(disable);
    }

    public void onQuitButton() {
        System.out.println("> Exit requested");
        Platform.exit();
    }

    public void onStartButton() {
        ServerConnection.setPlayerNickname(nicknameTextField.getText());
        Main.loadScene("Layout/Queue.fxml");
    }

    public void setRandomName() {
        nicknameTextField.setText(RANDOM_NAMES[new Random().nextInt(RANDOM_NAMES.length)]);
    }

}
