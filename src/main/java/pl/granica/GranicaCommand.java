package pl.granica;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class GranicaCommand implements CommandExecutor, TabCompleter {

    private static final List<String> PODKOMENDY = List.of(
            "ustaw", "srodek", "zmniejsz", "zatrzymaj", "reset", "info",
            "obrazenia", "ostrzezenieczas", "ostrzezenieodleglosc"
    );

    private final GranicaManager granica;

    public GranicaCommand(GranicaManager granica) {
        this.granica = granica;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            usage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (!sub.equals("info") && !sender.hasPermission("granica.admin")) {
            sender.sendMessage(Component.text("Nie masz uprawnień do tej komendy.", NamedTextColor.RED));
            return true;
        }
        if (sub.equals("info") && !sender.hasPermission("granica.info")) {
            sender.sendMessage(Component.text("Nie masz uprawnień do tej komendy.", NamedTextColor.RED));
            return true;
        }

        switch (sub) {
            case "ustaw" -> ustaw(sender, args);
            case "srodek" -> srodek(sender, args);
            case "zmniejsz" -> zmniejsz(sender, args);
            case "zatrzymaj" -> zatrzymaj(sender);
            case "reset" -> reset(sender);
            case "info" -> info(sender);
            case "obrazenia" -> obrazenia(sender, args);
            case "ostrzezenieczas" -> ostrzezenieczas(sender, args);
            case "ostrzezenieodleglosc" -> ostrzezenieodleglosc(sender, args);
            default -> usage(sender);
        }
        return true;
    }

    private void usage(CommandSender sender) {
        sender.sendMessage(Component.text("Użycie: /granica <podkomenda>", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  ustaw <rozmiar>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  srodek [x] [z]  - bez argumentów: Twoja pozycja", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  zmniejsz <rozmiar> <sekundy>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  zatrzymaj", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  reset", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  info", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  obrazenia <wartość>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  ostrzezenieczas <sekundy>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  ostrzezenieodleglosc <bloki>", NamedTextColor.GRAY));
    }

    private void ustaw(CommandSender sender, String[] args) {
        Double rozmiar = parseDouble(sender, args, 1, "Użycie: /granica ustaw <rozmiar>");
        if (rozmiar == null) return;
        if (rozmiar < 1) {
            sender.sendMessage(Component.text("Rozmiar musi być większy od zera.", NamedTextColor.RED));
            return;
        }

        granica.ustawRozmiar(rozmiar);
        sender.sendMessage(Component.text("Ustawiono rozmiar granicy na " + format(rozmiar) + ".", NamedTextColor.GREEN));
    }

    private void srodek(CommandSender sender, String[] args) {
        double x, z;

        if (args.length >= 3) {
            Double px = parseDouble(sender, args, 1, "Użycie: /granica srodek [x] [z]");
            if (px == null) return;
            Double pz = parseDouble(sender, args, 2, "Użycie: /granica srodek [x] [z]");
            if (pz == null) return;
            x = px;
            z = pz;
        } else if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text(
                        "Konsola musi podać współrzędne: /granica srodek <x> <z>", NamedTextColor.RED));
                return;
            }
            x = player.getLocation().getX();
            z = player.getLocation().getZ();
        } else {
            sender.sendMessage(Component.text("Użycie: /granica srodek [x] [z]", NamedTextColor.YELLOW));
            return;
        }

        granica.ustawSrodek(x, z);
        sender.sendMessage(Component.text(
                "Ustawiono środek granicy na X=" + format(x) + " Z=" + format(z) + ".", NamedTextColor.GREEN));
    }

    private void zmniejsz(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Użycie: /granica zmniejsz <rozmiar> <sekundy>", NamedTextColor.YELLOW));
            return;
        }

        Double rozmiar = parseDouble(sender, args, 1, "Użycie: /granica zmniejsz <rozmiar> <sekundy>");
        if (rozmiar == null) return;

        long sekundy;
        try {
            sekundy = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Liczba sekund musi być liczbą całkowitą.", NamedTextColor.RED));
            return;
        }

        if (rozmiar < 1 || sekundy < 0) {
            sender.sendMessage(Component.text("Rozmiar musi być większy od zera, a czas nie może być ujemny.", NamedTextColor.RED));
            return;
        }

        granica.zmniejszDo(rozmiar, sekundy);
        sender.sendMessage(Component.text(
                "Granica będzie zmieniać rozmiar do " + format(rozmiar) + " przez " + sekundy + " s.",
                NamedTextColor.GREEN));
    }

    private void zatrzymaj(CommandSender sender) {
        granica.zatrzymaj();
        sender.sendMessage(Component.text("Zatrzymano zmianę rozmiaru granicy.", NamedTextColor.GREEN));
    }

    private void reset(CommandSender sender) {
        granica.reset();
        sender.sendMessage(Component.text("Przywrócono domyślne ustawienia granicy.", NamedTextColor.GREEN));
    }

    private void info(CommandSender sender) {
        sender.sendMessage(legacy(granica.info()));
    }

    private void obrazenia(CommandSender sender, String[] args) {
        Double wartosc = parseDouble(sender, args, 1, "Użycie: /granica obrazenia <wartość>");
        if (wartosc == null) return;
        if (wartosc < 0) {
            sender.sendMessage(Component.text("Wartość nie może być ujemna.", NamedTextColor.RED));
            return;
        }

        granica.ustawObrazenia(wartosc);
        sender.sendMessage(Component.text("Ustawiono obrażenia na " + format(wartosc) + " / s.", NamedTextColor.GREEN));
    }

    private void ostrzezenieczas(CommandSender sender, String[] args) {
        Integer sekundy = parseInt(sender, args, 1, "Użycie: /granica ostrzezenieczas <sekundy>");
        if (sekundy == null) return;
        if (sekundy < 0) {
            sender.sendMessage(Component.text("Wartość nie może być ujemna.", NamedTextColor.RED));
            return;
        }

        granica.ustawCzasOstrzezenia(sekundy);
        sender.sendMessage(Component.text("Ustawiono czas ostrzeżenia na " + sekundy + " s.", NamedTextColor.GREEN));
    }

    private void ostrzezenieodleglosc(CommandSender sender, String[] args) {
        Integer bloki = parseInt(sender, args, 1, "Użycie: /granica ostrzezenieodleglosc <bloki>");
        if (bloki == null) return;
        if (bloki < 0) {
            sender.sendMessage(Component.text("Wartość nie może być ujemna.", NamedTextColor.RED));
            return;
        }

        granica.ustawOdlegloscOstrzezenia(bloki);
        sender.sendMessage(Component.text("Ustawiono odległość ostrzeżenia na " + bloki + " bloków.", NamedTextColor.GREEN));
    }

    private Double parseDouble(CommandSender sender, String[] args, int index, String usage) {
        if (args.length <= index) {
            sender.sendMessage(Component.text(usage, NamedTextColor.YELLOW));
            return null;
        }
        try {
            return Double.parseDouble(args[index]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("To nie jest poprawna liczba: " + args[index], NamedTextColor.RED));
            return null;
        }
    }

    private Integer parseInt(CommandSender sender, String[] args, int index, String usage) {
        if (args.length <= index) {
            sender.sendMessage(Component.text(usage, NamedTextColor.YELLOW));
            return null;
        }
        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("To nie jest poprawna liczba całkowita: " + args[index], NamedTextColor.RED));
            return null;
        }
    }

    private String format(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.1f", value);
    }

    private Component legacy(String tekst) {
        return LegacyComponentSerializer.legacySection().deserialize(tekst);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return PODKOMENDY.stream().filter(s -> s.startsWith(prefix)).toList();
        }
        return List.of();
    }
}
