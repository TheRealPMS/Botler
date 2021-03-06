package com.telegrambot.core;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MafiaBot extends TelegramLongPollingBot {

    private final int groupID = -364537563;
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Player> livingPlayers = new ArrayList<>();
    private ArrayList<String> activeRoles = new ArrayList<>();
    static ArrayList<String> availableRoles = new ArrayList<>();
    private ArrayList<Player> villagers = new ArrayList<>();
    private ArrayList<Player> goodGuys = new ArrayList<>();
    private ArrayList<Player> mafias = new ArrayList<>();
    private ArrayList<Player> badGuys = new ArrayList<>();
    private ArrayList<Player> lovers = new ArrayList<>();
    private ArrayList<Player> teamLove = new ArrayList<>();
    private ArrayList<Player> teamLivingLove = new ArrayList<>();
    private ArrayList<String> nominated = new ArrayList<>();
    private ArrayList<String> candidates = new ArrayList<>();
    private String major = "";
    private String protectedPlayer = "";
    private String revealedPlayer = "";
    private String targetPlayer = "";
    private String poisonedPlayer = "";
    private String terroredPlayer = "";
    private String trippedPlayer = "";
    private Boolean amorHasDecided = false;
    private Boolean hexeDecidedSaved = false;
    private Boolean hexeDecidedPoisoned = false;
    private Boolean leibwaechterDecided = false;
    private Boolean mafiaDecided = false;
    private Boolean detektivDecided = false;
    private Boolean drogendealerDecided = false;
    private Boolean daytime = true; //false ist Tag, true ist Nacht
    private Boolean gameRunning = false;
    private Boolean informDrogendealer = false;
    private Boolean informTerrorist = false;
    private Boolean firstNight = true;

    @Override
    public void onUpdateReceived(Update update) {
        String text = update.getMessage().getText();
        int id = update.getMessage().getFrom().getId();
        react(text, id);
    }

    private void react(String text, int id) {
        switch (text) {
            case "/nokill":
                mafiaDecided = true;
                new BotMessage(id, "Diese Nacht tötet die Mafia niemanden!").send();
                checkReady();
                break;
            case "/nacht":
                if (!gameRunning) {
                    new BotMessage(groupID, "Das Spiel läuft gar nicht!").send();
                    return;
                }
                nacht();
                break;

            case "/tag":
                if (!gameRunning) {
                    new BotMessage(groupID, "Das Spiel läuft gar nicht!").send();
                    return;
                }
                tag();
                break;

            case "/active":
                new BotMessage(groupID, getActiveRoles().toString()).send();
                break;
            case "/resetpunkte":
                new BotMessage(groupID, "Alle Punkte wurden zurückgesetzt!").send();
                for (Player player : players) {
                    player.resetPunkte();
                }
                break;
            case "/roles":
                new BotMessage(groupID, getAvailableRoles().toString()).send();
                break;
            case "/players":
                new BotMessage(groupID, getPlayers().toString()).send();
                break;

            case "/living":
                if (!gameRunning) {
                    new BotMessage(groupID, "Das Spiel läuft gar nicht!").send();
                    return;
                }
                new BotMessage(groupID, getLivingPlayers().toString()).send();
                break;

            case "/newgame":
                newGame();
                break;

            case "/candidate":
                if (dayLogicCheck(id)) return;
                if (!major.isEmpty()) {
                    new BotMessage(id, "Wir haben doch schon einen Bürgermeister!").send();
                    return;
                }
                if (candidates.contains(getPlayerById(id).getPlayerName())) {
                    new BotMessage(id, "Du hast dich schon zur Wahl auf den Posten des Bürgermeisters gestellt!").send();
                    return;
                }
                candidates.add(getPlayerById(id).getPlayerName());
                new BotMessage(groupID, getPlayerById(id).getPlayerName() + " hat sich soeben zur Wahl auf den Posten des Bürgermeisters gestellt").send();
                break;

            case "/informdrogendealer":
                informDrogendealer ^= true;
                if (informDrogendealer) {
                    new BotMessage(groupID, "Der Drogendealer wird ab dem nächsten Spiel informiert, wer die Mafiosi sind!").send();
                } else {
                    new BotMessage(groupID, "Der Drogendealer wird ab dem nächsten Spiel NICHT mehr informiert, wer die Mafiosi sind!").send();
                }
                break;
            case "/informterrorist":
                informTerrorist ^= true;
                if (informTerrorist) {
                    new BotMessage(groupID, "Der Terrorist wird ab dem nächsten Spiel informiert, wer die Mafiosi sind!").send();
                } else {
                    new BotMessage(groupID, "Der Terrorist wird ab dem nächsten Spiel NICHT mehr informiert, wer die Mafiosi sind!").send();
                }
                break;
            case "/getnominated":
                StringBuilder nominates = new StringBuilder();
                for (String nominate : nominated) {
                    nominates.append(nominate).append(": ").append(getPlayerByName(nominate).getVotesFor()).append("\n");
                }
                new BotMessage(groupID, "Das sind alle bisher Nominierten und die Zahl der bisher für sie abgegebenen Stimmen: \n" + nominates.toString()).send();
                break;

            case "/cancelgame":
                if (!gameRunning) {
                    new BotMessage(groupID, "Das Spiel läuft gar nicht!").send();
                    return;
                }
                new BotMessage(groupID, getPlayerById(id).getPlayerName() + " hat soeben das Spiel abgebrochen. Alles wurde zurückgesetzt!").send();
                reset();
                break;

            case "/getpunkte":
                StringBuilder tmp = new StringBuilder();
                for (Player player : players) {
                    tmp.append(player.getPlayerName()).append(": ").append(player.getPunkte()).append("\n");
                }
                new BotMessage(groupID, tmp.toString()).send();
                break;

            case "/whodead":
                if (!gameRunning) {
                    new BotMessage(groupID, "Das Spiel läuft gar nicht!").send();
                    return;
                }
                if (livingPlayers.size() == players.size()) {
                    new BotMessage(groupID, "Es gibt diese Runde noch gar keine Toten!").send();
                    return;
                }
                StringBuilder dead = new StringBuilder();
                for (Player player : players) {
                    if (!(livingPlayers.contains(player))) {
                        dead.append(player.getPlayerName()).append(": ").append(player.getRole()).append("\n");
                    }
                }
                new BotMessage(groupID, dead.toString()).send();
                break;

            case "/deregister":
                if (!(registerCheck(id))) {
                    new BotMessage(groupID, "Du bist gar nicht registriert!").send();
                    return;
                }
                for (Player player : players) {
                    if (player == getPlayerById(id)) {
                        new BotMessage(groupID, getPlayerById(id).getPlayerName() + " möchte doch nicht mitspielen, schade!").send();
                        players.remove(player);
                    }
                }
                break;

            case "/getcandidates":
                StringBuilder candidateString = new StringBuilder();
                for (String candidate : candidates) {
                    candidateString.append(candidate).append(": ").append(getPlayerByName(candidate).getMajorVotesFor()).append("\n");
                }
                new BotMessage(groupID, "Das sind alle bisher Kandidierenden auf das Amt des Bürgermeisters und die Zahl der bisher für sie abgegebenen Stimmen: \n" + candidateString.toString()).send();
                break;

            case "/save":
                if (commandLogicCheck(id)) return;
                if (!(getPlayerById(id).getRole().equals("Hexe"))) {
                    new BotMessage(id, "Du bist gar nicht die Hexe!").send();
                    return;
                }
                if (checkTrip(id)) return;
                if (targetPlayer.isEmpty()) {
                    new BotMessage(id, "Warte doch bitte, bis ich dir das Opfer sage!").send();
                    return;
                }
                if (hexeDecidedSaved) {
                    new BotMessage(id, "Du hast dich diese Nacht schon entschieden, ob du retten willst oder nicht!").send();
                    return;
                }
                if (!(getPlayerById(id).getLebenstrank())) {
                    new BotMessage(id, "Du hast den Lebenstrank doch gar nicht mehr!").send();
                    return;
                }
                new BotMessage(id, targetPlayer + " wird gerettet!").send();
                targetPlayer = "";
                getPlayerById(id).setLebenstrank();
                hexeDecidedSaved = true;
                checkReady();
                break;

            case "/nosave":
                if (commandLogicCheck(id)) return;
                if (!(getPlayerById(id).getRole().equals("Hexe"))) {
                    new BotMessage(id, "Du bist gar nicht die Hexe!").send();
                    return;
                }
                if (checkTrip(id)) return;
                if (targetPlayer.isEmpty()) {
                    new BotMessage(id, "Warte doch bitte, bis ich dir das Opfer sage!").send();
                    return;
                }
                if (hexeDecidedSaved) {
                    new BotMessage(id, "Du hast dich diese Nacht schon entschieden, ob du retten willst oder nicht!").send();
                    return;
                }
                new BotMessage(id, targetPlayer + " wird NICHT gerettet!").send();
                hexeDecidedSaved = true;
                checkReady();
                break;

            case "/nopoison":
                if (commandLogicCheck(id)) return;
                if (hexeLogicCheck(id)) return;
                if (checkTrip(id)) return;
                new BotMessage(id, "In dieser Nacht tötest du niemanden!").send();
                hexeDecidedPoisoned = true;
                checkReady();
                break;

            case "/forcedecision":
                if (!gameRunning) {
                    new BotMessage(groupID, "Das Spiel läuft gar nicht!").send();
                    return;
                }
                int secondMostVoted;
                ArrayList<Integer> voteNumbers = new ArrayList<>();
                for (Player livingPlayer : livingPlayers) {
                    voteNumbers.add(livingPlayer.getVotesFor());
                }
                voteNumbers.sort(Collections.reverseOrder());
                voteNumbers.remove(0);
                secondMostVoted = voteNumbers.get(0);
                Player mostVotedPlayer = getMostVotedPlayer();
                if (mostVotedPlayer.getVotesFor() == secondMostVoted) {
                    summarizeDay(true);
                } else {
                    summarizeDay(false);
                }
                break;
        }

        if (text.startsWith("/register")) {
            if (registerCheck(id)) {
                new BotMessage(groupID, "Du bist schon registriert!").send();
                return;
            }
            if (text.equals("/register")) {
                new BotMessage(groupID, "Du musst mir schon einen Namen geben!").send();
                return;
            }
            text = text.replace("/register ", "");
            registerPlayer(text, id);
        }

        if (text.startsWith("/deleterole")) {
            text = text.replace("/deleterole ", "");
            if (!(availableRoles.contains(text))) {
                new BotMessage(groupID, "Diese Rolle ist gar nicht aktiv. Achte bitte auf eine korrekte Schreibweise!").send();
                return;
            }
            activeRoles.remove(text);
            new BotMessage(groupID, text + " wurde von den Rollen entfernt!").send();
        }

        if (text.startsWith("/addrole")) {
            text = text.replace("/addrole ", "");
            if (!(availableRoles.contains(text))) {
                new BotMessage(groupID, "Die Rolle gibt es nicht oder ist noch nicht implementiert. Achte bitte auf eine korrekte Schreibweise!").send();
                return;
            }
            activeRoles.add(text);
            new BotMessage(groupID, text + " wurde zu den Rollen hinzugefügt!").send();
        }

        if (text.startsWith("/love")) {
            if (commandLogicCheck(id)) return;
            int amorId = getIdByRole("Amor");
            if (amorId != id) {
                new BotMessage(id, "Du bist gar nicht Amor!").send();
                return;
            }
            if (checkTrip(id)) return;
            if (!(lovers.isEmpty())) {
                new BotMessage(id, "Du hast schon jemanden verliebt!").send();
                return;
            }
            teamLove.add(getPlayerById(id));
            teamLivingLove.add(getPlayerById(id));
            text = text.replace("/love ", "");
            String[] loves = text.split("\\s+");
            if (loves[1].equals(loves[0])) {
                new BotMessage(id, "Ich brauche zwei verschiedene Personen!").send();
                return;
            }
            for (String love : loves) {
                for (Player livingPlayer : livingPlayers) {
                    if (livingPlayer.getPlayerName().equals(love)) {
                        lovers.add(livingPlayer);
                        teamLove.add(livingPlayer);
                        teamLivingLove.add(livingPlayer);
                    }
                }
            }
            if (lovers.size() != 2) {
                lovers.clear();
                new BotMessage(id, "Ich kenne eine oder beide Personen nicht oder es sind mehr als 2 Personen. Überprüfe bitte deine Eingabe und versuche es nochmal!").send();
                return;
            }
            StringBuilder tmps = new StringBuilder("\n");
            for (Player tmp : lovers) {
                tmps.append(tmp.getPlayerName()).append("\n");
            }
            new BotMessage(id, tmps.toString() + " sind die Liebenden und wurden informiert!").send();
            for (Player player : lovers) {
                new BotMessage(getPlayerByName(player.getPlayerName()).getPlayerId(), "Ein Geheimnis verrat ich dir, Amor hat dich im Visier. Und da gibt es noch was, das du wissen musst: Sein Pfeil fliegt zu auf deine Brust. Das Liebespaar lautet: " + tmps.toString()).send();
            }
            amorHasDecided = true;
            checkReady();
        }

        if (text.startsWith("/kill")) {
            if (commandLogicCheck(id)) return;
            if (!(getPlayerById(id).getRole().equals("Mafia"))) {
                new BotMessage(id, "Du gehörst gar nicht zu Mafia!").send();
                return;
            }
            if (checkTrip(id)) return;
            if (!(targetPlayer.isEmpty())) {
                new BotMessage(id, "Die Mafia hat diese Nacht schon ein Ziel ausgewählt!").send();
                return;
            }
            text = text.replace("/kill ", "");
            for (Player livingPlayer : livingPlayers) {
                if (livingPlayer.getPlayerName().equals(text)) {
                    targetPlayer = livingPlayer.getPlayerName();
                    new BotMessage(id, targetPlayer + " wurde als Ziel der Mafia ausgewählt!").send();
                    callHexe();
                }
            }
            if (targetPlayer.isEmpty()) {
                new BotMessage(id, "Diesen Spieler kenne ich gar nicht oder er ist schon tot. Probier es doch einfach nochmal!").send();
                return;
            }
            mafiaDecided = true;
            checkReady();
        }

        if (text.startsWith("/protect")) {
            if (commandLogicCheck(id)) return;
            if (getIdByRole("Leibwächter") != id) {
                new BotMessage(id, "Du bist gar nicht der Leibwächter!").send();
                return;
            }
            if (checkTrip(id)) return;
            if (!(protectedPlayer.isEmpty())) {
                new BotMessage(id, "Du hast diese Nacht schon jemanden beschützt!").send();
                return;
            }
            text = text.replace("/protect ", "");
            for (Player livingPlayer : livingPlayers) {
                if (livingPlayer.getPlayerName().equals(text)) {
                    protectedPlayer = livingPlayer.getPlayerName();
                    new BotMessage(id, protectedPlayer + " wird diese Nacht beschützt!").send();
                }
            }
            if (protectedPlayer.isEmpty()) {
                new BotMessage(id, "Diesen Spieler kenne ich gar nicht oder er ist schon tot. Probier es doch einfach nochmal!").send();
                return;
            }
            leibwaechterDecided = true;
            checkReady();
        }

        if (text.startsWith("/reveal")) {
            if (commandLogicCheck(id)) return;
            if (getIdByRole("Detektiv") != id) {
                new BotMessage(id, "Du bist gar nicht der Detektiv!").send();
                return;
            }
            if (checkTrip(id)) return;
            if (!(revealedPlayer.isEmpty())) {
                new BotMessage(id, "Du hast diese Nacht schon jemanden aufgedeckt!").send();
                return;
            }
            text = text.replace("/reveal ", "");
            for (Player livingPlayer : livingPlayers) {
                if (livingPlayer.getPlayerName().equals(text)) {
                    if (livingPlayer.getRole().equals("Mafia")) {
                        new BotMessage(id, "Die ausgewählte Person ist ein Mafiosi!").send();
                    } else {
                        new BotMessage(id, "Die ausgewählte Person ist KEIN Mafiosi!").send();
                    }
                    revealedPlayer = livingPlayer.getPlayerName();
                }
            }
            if (revealedPlayer.isEmpty()) {
                new BotMessage(id, "Diesen Spieler kenne ich gar nicht oder er ist schon tot. Probier es doch einfach nochmal!").send();
                return;
            }
            detektivDecided = true;
            checkReady();
        }

        if (text.startsWith("/poison")) {
            if (commandLogicCheck(id)) return;
            if (hexeLogicCheck(id)) return;
            if (checkTrip(id)) return;
            text = text.replace("/poison ", "");
            for (Player livingPlayer : livingPlayers) {
                if (livingPlayer.getPlayerName().equals(text)) {
                    poisonedPlayer = livingPlayer.getPlayerName();
                }
            }
            if (poisonedPlayer.isEmpty()) {
                new BotMessage(id, "Diesen Spieler kenne ich gar nicht oder er ist schon tot. Probier es doch einfach nochmal!").send();
                return;
            }
            new BotMessage(id, poisonedPlayer + " wurde vergiftet!").send();
            getPlayerById(id).setTodestrank();
            hexeDecidedPoisoned = true;
            checkReady();
        }

        if (text.startsWith("/terror")) {
            if (dayLogicCheck(id)) return;
            if (!(getPlayerById(id).getRole().equals("Terrorist"))) {
                new BotMessage(id, "Du bist gar nicht der Terrorist!").send();
                return;
            }
            text = text.replace("/terror ", "");
            if (text.equals(getPlayerById(id).getPlayerName())) {
                new BotMessage(id, "Willst du dich als Terrorist wirklich selbst hochsprengen?").send();
                return;
            }
            for (Player livingPlayer : livingPlayers) {
                if (livingPlayer.getPlayerName().equals(text)) {
                    terroredPlayer = livingPlayer.getPlayerName();
                }
            }
            if (terroredPlayer.isEmpty()) {
                new BotMessage(id, "Diesen Spieler kenne ich gar nicht oder er ist schon tot. Probier es doch einfach nochmal!").send();
                return;
            }
            new BotMessage(groupID, "Der Terrorist hat sich soeben hochgesprengt und er nimmt mit sich: " + terroredPlayer + " (" + getRoleByPlayerName(terroredPlayer) + ")").send();
            livingPlayers.removeIf(s -> s.getPlayerName().equals(terroredPlayer) || s.getPlayerName().equals(getPlayerByRole("Terrorist").getPlayerName()));
            for (Player player : livingPlayers) {
                if ((player.getPlayerName().equals(terroredPlayer)) || (player.getPlayerName().equals(getPlayerByRole("Terrorist").getPlayerName()))) {
                    removePlayer(player);
                }
            }
        }

        if (text.startsWith("/vote")) {
            if (dayLogicCheck(id)) return;
            if (major.isEmpty()) {
                new BotMessage(id, "Lass uns doch erstmal einen Bürgermeister wählen!").send();
                return;
            }
            if (!getPlayerById(id).getHasVotedFor().isEmpty()) {
                new BotMessage(groupID, "Du hast schon abgestimmt für " + getPlayerById(id).getHasVotedFor()).send();
                return;
            }
            text = text.replace("/vote ", "");
            for (Player livingPlayer : livingPlayers) {
                if (livingPlayer.getPlayerName().equals(text)) {
                    if (!(nominated.contains(livingPlayer.getPlayerName()))) {
                        new BotMessage(groupID, "Der Spieler ist gar nicht nominiert. Benutze dafür bitte /nominate NAME!").send();
                        return;
                    }
                    getPlayerById(id).setHasVotedFor(livingPlayer.getPlayerName());
                    break;
                }
            }
            if (getPlayerById(id).getHasVotedFor().isEmpty()) {
                new BotMessage(groupID, "Bitte schreib den Namen richtig, den kenne ich gar nicht, du retard!").send();
                return;
            }
            new BotMessage(groupID, getPlayerById(id).getPlayerName() + " hat soeben abgestimmt für: " + getPlayerById(id).getHasVotedFor()).send();
            getPlayerByName(getPlayerById(id).getHasVotedFor()).incrVotesFor();
            checkEnoughHaveVoted();
        }

        if (text.startsWith("/candidatevote")) {
            if (dayLogicCheck(id)) return;
            if (!major.isEmpty()) {
                new BotMessage(id, "Wir haben doch schon einen Bürgermeister!").send();
                return;
            }
            if (!getPlayerById(id).getHasVotedMajorFor().isEmpty()) {
                new BotMessage(groupID, "Du hast schon abgestimmt für " + getPlayerById(id).getHasVotedMajorFor()).send();
                return;
            }
            text = text.replace("/candidatevote ", "");
            for (Player livingPlayer : livingPlayers) {
                if (livingPlayer.getPlayerName().equals(text)) {
                    if (!(candidates.contains(livingPlayer.getPlayerName()))) {
                        new BotMessage(groupID, "Der Spieler hat sich gar nicht zur Wahl gestellt oder ich kenne ihn gar nicht. Derjenige muss erst aus eigenen Stücken kandidieren mit /candidate!").send();
                        return;
                    }
                    getPlayerById(id).setHasVotedMajorFor(livingPlayer.getPlayerName());
                    break;
                }
            }
            new BotMessage(groupID, getPlayerById(id).getPlayerName() + " hat soeben abgestimmt für: " + getPlayerById(id).getHasVotedMajorFor()).send();
            getPlayerByName(getPlayerById(id).getHasVotedMajorFor()).incrVotesMajorFor();
            checkEnoughHaveVotedMajor();
        }

        if (text.startsWith("/nominate")) {
            if (!gameRunning) {
                new BotMessage(groupID, "Das Spiel läuft gar nicht!").send();
                return;
            }
            if (!(livingPlayers.contains(getPlayerById(id)))) {
                new BotMessage(id, "Du lebst gar nicht mehr, sei ruhig!").send();
                return;
            }
            if (daytime) {
                new BotMessage(id, "Warte bitte auf den Tag!").send();
                return;
            }
            if (major.isEmpty()) {
                new BotMessage(id, "Lass uns doch erstmal einen Bürgermeister wählen!").send();
                return;
            }
            text = text.replace("/nominate ", "");
            if (nominated.contains(text)) {
                new BotMessage(groupID, "Der Spieler ist bereits nominiert!").send();
                return;
            }
            int tmp = nominated.size();
            for (Player livingPlayer : livingPlayers) {
                if (livingPlayer.getPlayerName().equals(text)) {
                    nominated.add(livingPlayer.getPlayerName());
                }
            }
            if (nominated.size() == tmp) {
                new BotMessage(id, "Diesen Spieler kenne ich gar nicht oder er ist schon tot. Probier es doch einfach nochmal!").send();
                return;
            }
            new BotMessage(groupID, getPlayerById(id).getPlayerName() + " hat soeben " + nominated.get(nominated.size() - 1) + " nominiert!").send();
        }

        if (text.startsWith("/trip")) {
            if (commandLogicCheck(id)) return;
            if (getIdByRole("Drogendealer") != id) {
                new BotMessage(id, "Du bist gar nicht der Drogendealer!").send();
                return;
            }
            if (!(trippedPlayer.isEmpty())) {
                new BotMessage(id, "Du hast diese Nacht schon jemanden auf einen Trip gesetzt!").send();
                return;
            }
            text = text.replace("/trip ", "");
            for (Player livingPlayer : livingPlayers) {
                if (livingPlayer.getPlayerName().equals(text)) {
                    trippedPlayer = livingPlayer.getPlayerName();
                    new BotMessage(id, trippedPlayer + " wird diese Nacht auf Drogen gesetzt!").send();
                    switch (getPlayerByName(trippedPlayer).getRole()) {
                        case "Mafia":
                            if (mafias.size() == 1) {
                                mafiaDecided = true;
                                if (getPlayerByRole("Hexe").getLebenstrank()) {
                                    new BotMessage(getPlayerByRole("Hexe").getPlayerId(), "Es gibt diese Nacht kein Opfer von der Mafia!").send();
                                    hexeDecidedSaved = true;
                                }
                                callHexe();
                            }
                            break;
                        case "Leibwächter":
                            leibwaechterDecided = true;
                            break;
                        case "Detektiv":
                            detektivDecided = true;
                            break;
                        case "Hexe":
                            hexeDecidedSaved = true;
                            hexeDecidedPoisoned = true;
                            break;
                        case "Amor":
                            amorHasDecided = true;
                            break;
                    }
                    new BotMessage(getPlayerByName(trippedPlayer).getPlayerId(), "Du bist diese Nacht auf Drogen und kannst nichts tun!").send();
                }
            }
            if (trippedPlayer.isEmpty()) {
                new BotMessage(id, "Diesen Spieler kenne ich gar nicht oder er ist schon tot. Probier es doch einfach nochmal!").send();
                return;
            }
            drogendealerDecided = true;
            checkReady();
            nacht();
        }
    }

    private void checkEnoughHaveVotedMajor() {
        int noVoteCount = 0;
        int secondMostVoted;
        ArrayList<Integer> voteNumbers = new ArrayList<>();
        for (Player livingPlayer : livingPlayers) {
            if (livingPlayer.getHasVotedMajorFor().isEmpty()) {
                noVoteCount++;
            }
            voteNumbers.add(livingPlayer.getMajorVotesFor());
        }
        voteNumbers.sort(Collections.reverseOrder());
        voteNumbers.remove(0);
        secondMostVoted = voteNumbers.get(0);
        Player player = getMostVotedMajor();
        if ((player.getMajorVotesFor() - secondMostVoted) > noVoteCount) {
            summarizeMajorElection();
        }
        if (noVoteCount == 0) {
            summarizeMajorElection();
        }
    }

    private void summarizeMajorElection() {
        Player player = getMostVotedMajor();
        StringBuilder nominates = new StringBuilder("Die Wahlen für den Bürgermeister ist entschieden. Die Kandidaten und Stimmen im Überblick: \n");
        for (String candidate : candidates) {
            nominates.append(candidate).append(": ").append(getPlayerByName(candidate).getMajorVotesFor()).append("\n");
        }
        new BotMessage(groupID, nominates.toString()).send();
        new BotMessage(groupID, "Unser neuer Bürgermeister ist " + player.getPlayerName() + ". Herzlichen Glückwunsch!").send();
        new BotMessage(groupID, "Damit ist die Abstimmung eröffnet!").send();
        major = player.getPlayerName();
        for (Player tmp : players) {
            tmp.resetMajorVotesFor();
            tmp.setHasVotedMajorFor("");
        }
        candidates.clear();
    }

    private Player getMostVotedMajor() {
        Player player = getPlayerByName(candidates.get(0));
        for (String candidate : candidates) {
            if (getPlayerByName(candidate).getMajorVotesFor() > player.getMajorVotesFor()) {
                player = getPlayerByName(candidate);
            }
        }
        return player;
    }

    private boolean checkTrip(int id) {
        if (trippedPlayer.isEmpty() && livingPlayers.contains(getPlayerByRole("Drogendealer"))) {
            new BotMessage(id, "Warte bitte bis ich dich aufrufe!").send();
            return true;
        }
        if (getPlayerById(id).getPlayerName().equals(trippedPlayer)) {
            new BotMessage(id, "Du kannst nichts tun, weil du auf Drogen bist!").send();

            return true;
        }
        return false;
    }

    private boolean dayLogicCheck(int id) {
        if (generateErrorLogicCheck(id)) return true;
        if (daytime) {
            new BotMessage(id, "Warte bitte auf den Tag!").send();
            return true;
        }
        return false;
    }

    private boolean hexeLogicCheck(int id) {
        if (!(getPlayerById(id).getRole().equals("Hexe"))) {
            new BotMessage(id, "Du bist gar nicht die Hexe!").send();
            return true;
        }
        if (hexeDecidedPoisoned) {
            new BotMessage(id, "Du hast dich diese Nacht schon entschieden, ob du töten willst oder nicht!").send();
            return true;
        }
        return false;
    }

    private boolean commandLogicCheck(int id) {
        if (generateErrorLogicCheck(id)) return true;
        if (!daytime) {
            new BotMessage(id, "Warte bitte auf die Nacht!").send();
            return true;
        }
        return false;
    }

    private boolean generateErrorLogicCheck(int id) {
        if (!gameRunning) {
            new BotMessage(id, "Das Spiel läuft gar nicht!").send();
            return true;
        }
        if (!(livingPlayers.contains(getPlayerById(id)))) {
            new BotMessage(id, "Du lebst gar nicht mehr, sei ruhig!").send();
            return true;
        }
        return false;
    }

    private void checkEnoughHaveVoted() {
        int noVoteCount = 0;
        int secondMostVoted;
        ArrayList<Integer> voteNumbers = new ArrayList<>();
        for (Player livingPlayer : livingPlayers) {
            if (livingPlayer.getHasVotedFor().isEmpty()) {
                noVoteCount++;
            }
            voteNumbers.add(livingPlayer.getVotesFor());
        }
        voteNumbers.sort(Collections.reverseOrder());
        voteNumbers.remove(0);
        secondMostVoted = voteNumbers.get(0);
        Player mostVotedPlayer = getMostVotedPlayer();
        if ((mostVotedPlayer.getVotesFor() - secondMostVoted) > noVoteCount) {
            summarizeDay(false);
            return;
        }
        if (noVoteCount == 0) {
            summarizeDay(true);
        }
    }

    private void summarizeDay(Boolean equal) {
        Player player = getMostVotedPlayer();
        if (equal) {
            player = getPlayerByName(getPlayerByName(major).getHasVotedFor());
        }
        StringBuilder nominates = new StringBuilder("Die Abstimmung ist entschieden. Die Nominierten und Stimmen im Überblick: \n");
        for (String nominate : nominated) {
            nominates.append(nominate).append(": ").append(getPlayerByName(nominate).getVotesFor()).append("\n");
        }
        new BotMessage(groupID, nominates.toString()).send();
        if (equal) {
            new BotMessage(groupID, "Bei dieser Abstimmung gab es einen Gleichstand, also entscheidet die Stimme des Bürgermeisters!").send();
        }
        new BotMessage(groupID, "Getötet wird " + player.getPlayerName() + " (" + player.getRole() + ")").send();
        if (!badGuys.contains(player)) {
            new BotMessage(groupID, "Könnt ihr eigentlich irgendwas, ihr Kacknoobs?").send();
        }
        removePlayer(player);
        if (livingPlayers.isEmpty()) {
            return;
        }
        nominated.clear();
        for (Player tmp : players) {
            tmp.resetVotesFor();
            tmp.setHasVotedFor("");
        }
        vorNacht();
    }

    private Player getMostVotedPlayer() {
        Player player = getPlayerByName(nominated.get(0));
        for (String nominate : nominated) {
            if (getPlayerByName(nominate).getVotesFor() > player.getVotesFor()) {
                player = getPlayerByName(nominate);
            }
        }
        return player;
    }

    private void removePlayer(Player player) {
        livingPlayers.remove(player);
        mafias.remove(player);
        villagers.remove(player);
        teamLivingLove.remove(player);
        if (major.equals(player.getPlayerName())) {
            major = "";
        }
        if (lovers.contains(player)) {
            lovers.remove(player);
            Player secondLover = lovers.get(0);
            new BotMessage(groupID, "Soeben ist noch jemand gestorben, denn " + player.getPlayerName() + " war verliebt mit " + secondLover.getPlayerName() + "!\nDamit geht auch von uns: " + secondLover.getPlayerName() + " (" + secondLover.getRole() + ")").send();
            lovers.clear();
            removePlayer(secondLover);
            return;
        }
        checkVictory();
    }

    private void checkVictory() {
        if (teamLivingLove.containsAll(livingPlayers)) {
            new BotMessage(groupID, "Alle noch lebenden Spieler gehören zum Team Liebe, damit hat die Liebe gewonnen, herzlichen Glückwunsch!").send();
            for (Player lover : teamLove) {
                lover.incrPunkte();
            }
            reset();
            return;
        }
        if (mafias.isEmpty()) {
            new BotMessage(groupID, "Alle Mafiosi sind tot, damit hat das Dorf gewonnen, herzlichen Glückwunsch!").send();
            for (Player goodGuy : goodGuys) {
                goodGuy.incrPunkte();
            }
            reset();
            return;
        }
        if (villagers.isEmpty()) {
            new BotMessage(groupID, "Alle Dorfbewohner sind tot, damit hat die Mafia gewonnen, herzlichen Glückwunsch!").send();
            for (Player badGuy : badGuys) {
                badGuy.incrPunkte();
            }
            reset();
        }
    }

    private void reset() {
        livingPlayers.clear();
        mafias.clear();
        badGuys.clear();
        lovers.clear();
        villagers.clear();
        teamLove.clear();
        teamLivingLove.clear();
        amorHasDecided = false;
        daytime = true;
        major = "";
        protectedPlayer = "";
        revealedPlayer = "";
        targetPlayer = "";
        poisonedPlayer = "";
        trippedPlayer = "";
        resetDuplicated();
    }

    private void resetDuplicated() {
        detektivDecided = false;
        drogendealerDecided = false;
        mafiaDecided = false;
        firstNight = false;

        hexeDecidedSaved = false;
        hexeDecidedPoisoned = false;
        daytime = false;
        gameRunning = false;
    }


    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for (Update update : updates) {
            String text = update.getMessage().getText();
            int id = update.getMessage().getFrom().getId();
            react(text, id);
        }
    }

    private void registerPlayer(String name, int id) {
        ArrayList temp = getPlayers();
        if (temp.contains(name)) {
            new BotMessage(groupID, "Der Spieler ist schon registriert!").send();
        } else {
            Player player = new Player(name, id);
            players.add(player);
            new BotMessage(groupID, player.getPlayerName() + " möchte mitspielen!").send();
        }
    }

    private void newGame() {
        gameRunning = true;
        livingPlayers.clear();
        livingPlayers.addAll(players);
        Collections.shuffle(activeRoles);
        if (livingPlayers.size() != activeRoles.size()) {
            new BotMessage(groupID, "Die Anzahl der Spieler muss gleich der Anzahl der aktiven Rollen sein!").send();
            return;
        }
        for (int i = 0; i < livingPlayers.size(); i++) {
            livingPlayers.get(i).setRole(activeRoles.get(i));
            new BotMessage(livingPlayers.get(i).getPlayerId(), "Du hast die Rolle: " + livingPlayers.get(i).getRole()).send();
        }
        new BotMessage(groupID, "Das Spiel hat begonnen. Ich habe jedem seine Rolle privat zugesendet! \nWir starten mit der ersten Nacht!").send();
        if (activeRoles.contains("Hexe")) {
            Player temp = getPlayerByRole("Hexe");
            temp.fillPotions();
        }
        for (Player livingPlayer : livingPlayers) {
            if (livingPlayer.getRole().equals("Mafia")) {
                mafias.add(livingPlayer);
                badGuys.add(livingPlayer);
            }
        }
        for (Player livingPlayer : livingPlayers) {
            if (livingPlayer.getRole().equals("Terrorist") || livingPlayer.getRole().equals("Drogendealer")) {
                badGuys.add(livingPlayer);
            }
        }
        for (Player livingPlayer : livingPlayers) {
            if (!(livingPlayer.getRole().equals("Mafia")) && !(livingPlayer.getRole().equals("Terrorist")) && !(livingPlayer.getRole().equals("Drogendealer"))) {
                villagers.add(livingPlayer);
                goodGuys.add(livingPlayer);
            }
        }
        if (mafias.size() > 1) {
            StringBuilder mafiosi = getMafiosi();
            for (Player mafia : mafias) {
                new BotMessage(mafia.getPlayerId(), "Mafiosi sind: " + mafiosi.toString()).send();
            }
        }
        if (informTerrorist && activeRoles.contains("Terrorist")) {
            StringBuilder mafiosi = getMafiosi();
            new BotMessage(getPlayerByRole("Terrorist").getPlayerId(), "Mafiosi sind: " + mafiosi.toString()).send();
        }
        if (informDrogendealer && activeRoles.contains("Drogendealer")) {
            StringBuilder mafiosi = getMafiosi();
            new BotMessage(getPlayerByRole("Drogendealer").getPlayerId(), "Mafiosi sind: " + mafiosi.toString()).send();
        }
        if (livingPlayers.contains(getPlayerByRole("Drogendealer"))) {
            vorNacht();
            return;
        }
        nacht();
    }

    private StringBuilder getMafiosi() {
        StringBuilder mafiosi = new StringBuilder("\n");
        for (Player mafia : mafias) {
            mafiosi.append(mafia.getPlayerName()).append("\n");
        }
        return mafiosi;
    }

    private void callAmor() {
        if (trippedPlayer.equals(getPlayerByRole("Amor").getPlayerName())) {
            return;
        }
        new BotMessage(getPlayerByRole("Amor").getPlayerId(), "Wen möchtest du verlieben? Nutze dafür das Command /love NAME NAME").send();
    }

    private void callHexe() {
        Player tmp = getPlayerByRole("Hexe");
        if (tmp.getPlayerName().equals(trippedPlayer)) {
            return;
        }
        if (tmp.getLebenstrank() && !hexeDecidedSaved) {
            new BotMessage(tmp.getPlayerId(), "Die Mafia hat ein Opfer gefunden. Das Opfer dieser Nacht ist " + targetPlayer + ".\nFalls du das Opfer retten möchtest, nutze /save, falls du dies NICHT tun willst, nutze /nosave!").send();
        }
        if (tmp.getTodestrank()) {
            new BotMessage(tmp.getPlayerId(), "Falls du diese Nacht jemanden töten möchtest, nutze /poison NAME, falls du dies NICHT tun willst, nutze /nopoison").send();
        }
        if (!tmp.getTodestrank() && !tmp.getLebenstrank()) {
            hexeDecidedSaved = true;
            hexeDecidedPoisoned = true;
            checkReady();
        }
    }

    private void vorNacht() {
        if (!gameRunning) {
            return;
        }
        daytime = true;
        String tmp = getPlayerByRole("Drogendealer").getPlayerName();
        if (livingPlayers.contains(getPlayerByRole("Drogendealer"))) {
            new BotMessage(groupID, "Es ist Nacht! Jeder schreibt mir jetzt bitte privat, was er diese Nacht tun möchte, wenn ich ihn dazu auffordere!").send();
        }
        if (livingPlayers.contains(getPlayerByName(tmp))) {
            new BotMessage(getPlayerByRole("Drogendealer").getPlayerId(), "Wen möchtest du diese Nacht auf Drogen setzen? Nutze dafür /trip NAME!").send();
        } else {
            nacht();
        }
    }

    private void nacht() {
        if (!gameRunning) {
            return;
        }
        if (!(livingPlayers.contains(getPlayerByRole("Drogendealer")))) {
            daytime = true;
            new BotMessage(groupID, "Es ist Nacht! Jeder schreibt mir jetzt bitte privat, was er diese Nacht tun möchte, wenn ich ihn dazu auffordere!").send();
        }
        for (Player livingPlayer : livingPlayers) {
            switch (livingPlayer.getRole()) {
                case "Mafia":
                    if (!mafiaDecided) {
                        new BotMessage(livingPlayer.getPlayerId(), "Wen möchte die Mafia umbringen? Sprich dich bitte mit deinen Kumpanen ab, ich zähle nur den ersten Befehl! Nutze dafür den Befehl /kill NAME").send();
                    }
                    break;
                case "Leibwächter":
                    if (!leibwaechterDecided) {
                        new BotMessage(livingPlayer.getPlayerId(), "Wen möchtest du diese Nacht vor den Angriffen der Mafia beschützen? Nutze dafür den Command /protect NAME").send();
                    }
                    break;
                case "Detektiv":
                    if (!detektivDecided) {
                        new BotMessage(livingPlayer.getPlayerId(), "Wessen Identität möchtest du diese Nacht erfahren? Nutze dafür den Command /reveal NAME").send();
                    }
                    break;
                case "Hexe":
                    if (!hexeDecidedSaved && !hexeDecidedPoisoned) {
                        if (!(livingPlayer.getLebenstrank())) {
                            hexeDecidedSaved = true;
                            if (livingPlayer.getTodestrank()) {
                                new BotMessage(livingPlayer.getPlayerId(), "Falls du diese Nacht jemanden töten möchtest, nutze /poison NAME, falls du dies NICHT tun willst, nutze /nopoison").send();
                            } else {
                                new BotMessage(livingPlayer.getPlayerId(), "Du hast leider keine Tränke mehr!").send();
                                hexeDecidedPoisoned = true;
                            }
                        } else {
                            if (!hexeDecidedSaved) {
                                new BotMessage(livingPlayer.getPlayerId(), "Ich werde dich informieren, wenn die Mafia ein Opfer gefunden hat!").send();
                            }
                        }
                    }
                    break;
                case "Amor":
                    if (!amorHasDecided || firstNight) {
                        callAmor();
                    }
                    break;
            }
        }
    }

    private void tag() {
        resetDuplicated();
        if (teamLove.isEmpty()) {
            amorHasDecided = false;
        }
        new BotMessage(groupID, "Guten Morgen zu einem neuen Tag in unserem Dorf!").send();
        ArrayList<String> tmpNames = new ArrayList<>();
        if (!poisonedPlayer.isEmpty()) {
            tmpNames.add(poisonedPlayer);
        }
        if (!targetPlayer.isEmpty() && !targetPlayer.equals(protectedPlayer)) {
            tmpNames.add(targetPlayer);
        }
        if (!tmpNames.isEmpty()) {
            new BotMessage(groupID, "Es gab in dieser Nacht Tote zu beklagen, gestorben sind:").send();
            for (String tmpName : tmpNames) {
                new BotMessage(groupID, tmpName + " (" + getRoleByPlayerName(tmpName) + ")").send();
                for (Player player : livingPlayers) {
                    if (player.getPlayerName().equals(tmpName)) {
                        removePlayer(player);
                        if (livingPlayers.isEmpty()) {
                            return;
                        }
                    }
                }
            }
        } else {
            new BotMessage(groupID, "Wir haben diese Nacht keine Toten zu beklagen!").send();
        }
        protectedPlayer = "";
        revealedPlayer = "";
        targetPlayer = "";
        poisonedPlayer = "";
        trippedPlayer = "";
        checkVictory();
        if (major.isEmpty() && gameRunning) {
            new BotMessage(groupID, "Als erstes brauchen wir einen Bürgermeister! Wer sich selbst zur Wahl stellen möchte, nutzt bitte /candidate. Um für einen Kandidaten abzustimmen, nutzt bitte /candidatevote!").send();
        }
    }

    private Boolean registerCheck(int id) {
        boolean tmp = false;
        for (Player player : players) {
            if (player.getPlayerId() == id) {
                tmp = true;
            }
        }
        return tmp;
    }

    private void checkReady() {
        if ((leibwaechterDecided || (!(livingPlayers.contains(getPlayerByRole("Leibwächter"))))) && (detektivDecided || (!(livingPlayers.contains(getPlayerByRole("Detektiv"))))) && (amorHasDecided || (!(livingPlayers.contains(getPlayerByRole("Amor"))))) && ((hexeDecidedSaved && hexeDecidedPoisoned) || (!(livingPlayers.contains(getPlayerByRole("Hexe"))))) && (mafiaDecided || (!(livingPlayers.contains(getPlayerByRole("Mafia"))))) && (drogendealerDecided || (!(livingPlayers.contains(getPlayerByRole("Drogendealer")))))) {
            tag();
        }
    }

    private int getIdByRole(String role) {
        for (Player player : players) {
            if (player.getRole().equals(role)) {
                return player.getPlayerId();
            }
        }
        new BotMessage(groupID, "NullPointerException in getIdByRole geworfen! Da ist irgendwas schief gegangen..").send();
        throw new NullPointerException("Tried to get a player who doesn't exist");
    }

    private Player getPlayerById(int id) {
        for (Player player : players) {
            if (player.getPlayerId() == id) {
                return player;
            }
        }
        new BotMessage(groupID, "NullPointerException in getPlayerById geworfen! Da ist irgendwas schief gegangen..").send();
        throw new NullPointerException("Tried to get a player who doesn't exist");
    }

    private Player getPlayerByName(String name) {
        for (Player player : players) {
            if (player.getPlayerName().equals(name)) {
                return player;
            }
        }
        new BotMessage(groupID, "NullPointerException in getPlayerByName geworfen! Da ist irgendwas schief gegangen..").send();
        throw new NullPointerException("Tried to get a player who doesn't exist");
    }

    private Player getPlayerByRole(String role) {
        for (Player player : players) {
            if (player.getRole().equals(role)) {
                return player;
            }
        }
        new BotMessage(groupID, "NullPointerException in getPlayerByRole geworfen! Da ist irgendwas schief gegangen..").send();
        throw new NullPointerException("Tried to get a player who doesn't exist");
    }

    private String getRoleByPlayerName(String name) {
        for (Player player : players) {
            if (player.getPlayerName().equals(name)) {
                return player.getRole();
            }
        }
        new BotMessage(groupID, "NullPointerException in getRoleByPlayerName geworfen! Da ist irgendwas schief gegangen..").send();
        throw new NullPointerException("Tried to get a player who doesn't exist");
    }

    private ArrayList getPlayers() {
        return getArrayList(players);
    }

    private ArrayList getLivingPlayers() {
        return getArrayList(livingPlayers);
    }

    private ArrayList getArrayList(ArrayList<Player> livingPlayers) {
        ArrayList<String> tempArray = new ArrayList<>();
        for (Player player : livingPlayers) {
            String name = player.getPlayerName();
            tempArray.add(name);
        }
        return tempArray;
    }

    private ArrayList getActiveRoles() {
        return activeRoles;
    }

    private static ArrayList getAvailableRoles() {
        return availableRoles;
    }

    @Override
    public String getBotUsername() {
        return "MafiaBossBot";
    }

    @Override
    public String getBotToken() {
        return "757130944:AAGLQzA69X4XpB3Flf8Ox3lajC8qUbXHkHo";
    }
}
