package com.grizzly.TheRiseAndFall.games;

import com.grizzly.TheRiseAndFall.Main;
import com.grizzly.TheRiseAndFall.gui.GameMenu;
import com.grizzly.TheRiseAndFall.gui.SnakeGUI;
import com.grizzly.TheRiseAndFall.util.ItemMaker;
import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class Snake {

    Player player;
    Inventory screen;
    List<Integer> food;
    List<Integer> head;
    List<List<Integer>> body;
    int score;
    int dir;
    int speed;
    boolean lose;
    boolean win;
    String lossReason;
    List<Integer> slots = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
            19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
            41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53));

    public Snake(Player player) {
        this.player = player;
        if (db.runningSnakeGUIs.containsKey(player.getUniqueId())) reloadVars();
         else {
            score = 0;
            dir = -1;
            screen = new SnakeGUI().getInventory();
            head = new ArrayList<>(Arrays.asList(4, 2));
            body = new ArrayList<>();
            body.add(new ArrayList<>(Arrays.asList(4, 0)));
            body.add(new ArrayList<>(Arrays.asList(4, 1)));
            food = newFoodSlot();
            lose = false;
            lossReason = "";
            win = false;
            speed = 0;
        } save();
    }

    void reloadVars() {
        Snake snake = db.runningSnakeGUIs.get(player.getUniqueId());
        screen = snake.screen;
        food = snake.food;
        head = snake.head;
        body = snake.body;
        score = snake.score;
        dir = snake.dir;
        lose = snake.lose;
        lossReason = snake.lossReason;
        win = snake.win;
        speed = snake.speed;
    }

    public int getSpeed() { return speed; }

    public void selectSpeed() {
        player.openInventory(new SnakeGUI().getSpeedInventory());
        Inventory screen = player.getOpenInventory().getTopInventory();
        if (player.getOpenInventory().getTitle().equals("§6§lSelect your speed")) {
            for (int i = 0; i < 27; i++) screen.setItem(i, new ItemMaker().createBlank());
            screen.setItem(11, new ItemMaker().create("§aSlow", Material.LIME_WOOL, Collections.singletonList("§7§o3 FPS")));
            screen.setItem(13, new ItemMaker().create("§eMedium", Material.YELLOW_WOOL, Collections.singletonList("§7§o5 FPS")));
            screen.setItem(15, new ItemMaker().create("§cFast", Material.RED_WOOL, Collections.singletonList("§7§o10 FPS")));
        }
    }

    public void start(int speed) {
        db.saveInventory(player);
        this.speed = speed;
        save();
        player.openInventory(screen);
        update(true);
        startScreen();
    }

    void startScreen() {
        Main plugin = Main.plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                reloadVars();
                db.runningSnakeGames.put(player.getUniqueId(), getTaskId());
                if (dir != -1) {
                    body.add(head);
                    int headX;
                    int headY;
                    if (dir == 0 || dir == 1) {
                        headX = head.get(0);
                        if (dir == 0) headY = head.get(1) + 1;
                        else headY = head.get(1) - 1;
                    } else {
                        headY = head.get(1);
                        if (dir == 2) headX = head.get(0) - 1;
                        else headX = head.get(0) + 1;
                    } head = new ArrayList<>(Arrays.asList(headX, headY));
                    if (score == 50) {
                        gameEnd(false, "");
                        return;
                    } if (headX < 0 || headX > 8 || headY < 0 || headY > 5) {
                        gameEnd(true, "You hit the wall!");
                        return;
                    } if (head.equals(food)) {
                        score++;
                        food = newFoodSlot();
                    } else body.remove(0);
                    if (body.contains(head)) {
                        gameEnd(true, "You hit yourself!");
                        return;
                    } update(false);
                }
            }
        }.runTaskTimer(plugin, 0, speed);
    }

     void stopScreen() {
        Bukkit.getScheduler().cancelTask(db.runningSnakeGames.get(player.getUniqueId()));
        db.runningSnakeGames.remove(player.getUniqueId());
    }

    void update(boolean firstTime) {
        if (firstTime) {
            Inventory inv = player.getInventory();
            String[] head = new String[]{"up", "left", "blank", "right", "down"};
            Integer[] slot = new Integer[]{13, 21, 22, 23, 31};
            for (int i = 0; i < 5; i++) inv.setItem(slot[i], new ItemMaker().head(head[i]));
        } Inventory screen = player.getOpenInventory().getTopInventory();
        for (int i = 0; i < 54; i++) { screen.setItem(i, null); }
        screen.setItem(coordsToSlot(food), object("food"));
        screen.setItem(coordsToSlot(head), object("head"));
        for (List<Integer> body : this.body) screen.setItem(coordsToSlot(body), object("body"));
        save();
    }

    void save() { db.runningSnakeGUIs.put(player.getUniqueId(), this); }

    List<Integer> newFoodSlot() {
        List<Integer> slotTryList = slots;
        Collections.shuffle(slotTryList);
        int slot = slotTryList.get(0);
        List<Integer> checkSlots = multipleCoordsToSlot(body);
        checkSlots.add(coordsToSlot(head));
        while (checkSlots.contains(slot)) {
            slotTryList.remove(0);
            slot = slotTryList.get(0);
        } return slotToCoords(slot);
    }

    ItemStack object(String type) {
        switch (type) {
            case "head":
                return new ItemMaker().create("§2Head", Material.GREEN_WOOL, Collections.singletonList("§7§oPart of your snake!"));
            case "body":
                return new ItemMaker().create("§aBody", Material.LIME_WOOL, Collections.singletonList("§7§oPart of your snake!"));
            case "food":
                return new ItemMaker().create("§eFood", Material.SUNFLOWER, Arrays.asList("§7§oFood for your snake!", "§7§oEat this to grow."));
            default:
                return new ItemMaker().create("§cError", Material.BARRIER, Collections.singletonList("§7§oIf you see this, an error has occured!"));
        }
    }

    int coordsToSlot(List<Integer> coords) {
        int slot = coords.get(0);
        slot += 45 - (coords.get(1)*9);
        return slot;
    }

    List<Integer> multipleCoordsToSlot(List<List<Integer>> coords) {
        List<Integer> slotsList = new ArrayList<>();
        for (List<Integer> coord : coords) {
            int slot = coord.get(0);
            slot += 45 - (coord.get(1)*9);
            slotsList.add(slot);
        } return slotsList;
    }

    List<Integer> slotToCoords(int slot) {
        List<Integer> coords = new ArrayList<>();
        coords.add(slot - (slot/9)*9);
        coords.add(5 - (slot/9));
        return coords;
    }

    void gameEnd(Boolean loss, String lossReason) {
        if (loss) lose = true;
        else win = true;
        this.lossReason = lossReason;
        save();
        reloadVars();
        player.closeInventory();
    }

    public void quit() {
        Main plugin = Main.plugin;
        stopScreen();
        if (!lose) {
            if (!win) player.sendMessage(Plugin.prefix + "§cYou quit! Score: §e" + score);
            else player.sendMessage(Plugin.prefix + "§aYou win!");
        } else player.sendMessage(Plugin.prefix + "§c" + lossReason + " Score: §e" + score);
        db.runningSnakeGUIs.remove(player.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> new GameMenu().open(player));
        Bukkit.getScheduler().runTaskLater(plugin, () -> db.loadInventoryIfBackedUp(player), 2);
    }

    public void setDir(int dir) {
        this.dir = dir;
        save();
    }

}
