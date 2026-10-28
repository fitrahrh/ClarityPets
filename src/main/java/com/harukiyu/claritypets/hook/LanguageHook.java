package com.harukiyu.claritypets.hook;

import com.harukiyu.claritylangcore.api.ClarityLangAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class LanguageHook {

    private static Plugin pluginInstance;
    private static String namespace = "pets";
    private static boolean isClarityLangCorePresent = false;
    private static final Map<String, YamlConfiguration> fallbackBundles = new ConcurrentHashMap<>();
    private static final Map<String, YamlConfiguration> jarBundles = new ConcurrentHashMap<>();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static final List<String> SUPPORTED_LANGS = List.of(
            "en_US", "id_ID", "pt_BR", "es_ES", "zh_CN", "ja_JP", "de_DE", "fr_FR", "ru_RU"
    );

    private LanguageHook() {}

    public static void init(@NotNull Plugin plugin, @NotNull String ns) {
        pluginInstance = plugin;
        namespace = ns.toLowerCase(Locale.ROOT);
        loadBundles();

        if (Bukkit.getPluginManager().isPluginEnabled("ClarityLangCore")) {
            try {
                ClarityLangAPI.registerBundle(plugin, namespace, "languages");
                File langFolder = new File(plugin.getDataFolder(), "languages");
                if (langFolder.exists()) {
                    try {
                        ClarityLangAPI.registerBundle(namespace, langFolder);
                    } catch (Throwable ignored) {}
                }
                isClarityLangCorePresent = true;
                plugin.getLogger().log(Level.INFO, "Successfully hooked into ClarityLangCore bundle [{0}]!", namespace);
            } catch (Throwable t) {
                isClarityLangCorePresent = false;
            }
        } else {
            isClarityLangCorePresent = false;
        }
    }

    public static void reload() {
        loadBundles();
        if (pluginInstance != null && Bukkit.getPluginManager().isPluginEnabled("ClarityLangCore")) {
            try {
                ClarityLangAPI.registerBundle(pluginInstance, namespace, "languages");
                File langFolder = new File(pluginInstance.getDataFolder(), "languages");
                if (langFolder.exists()) {
                    ClarityLangAPI.registerBundle(namespace, langFolder);
                }
            } catch (Throwable ignored) {}
        }
    }

    private static void loadBundles() {
        fallbackBundles.clear();
        jarBundles.clear();

        if (pluginInstance == null) return;

        File langFolder = new File(pluginInstance.getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        for (String lang : SUPPORTED_LANGS) {
            String lowerLang = lang.toLowerCase(Locale.ROOT);
            File langFile = new File(langFolder, lang + ".yml");
            if (!langFile.exists()) {
                try {
                    pluginInstance.saveResource("languages/" + lang + ".yml", false);
                } catch (Throwable ignored) {}
            }

            YamlConfiguration jarConfig = null;
            try (InputStream stream = pluginInstance.getResource("languages/" + lang + ".yml")) {
                if (stream != null) {
                    jarConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    jarBundles.put(lowerLang, jarConfig);
                }
            } catch (IOException ignored) {}

            YamlConfiguration diskConfig = null;
            if (langFile.exists()) {
                diskConfig = YamlConfiguration.loadConfiguration(langFile);
                if (jarConfig != null) {
                    boolean modified = false;
                    for (String key : jarConfig.getKeys(true)) {
                        if (!diskConfig.contains(key)) {
                            diskConfig.set(key, jarConfig.get(key));
                            modified = true;
                        }
                    }
                    if (modified) {
                        try {
                            diskConfig.save(langFile);
                        } catch (IOException ignored) {}
                    }
                    diskConfig.setDefaults(jarConfig);
                }
            } else if (jarConfig != null) {
                diskConfig = jarConfig;
            }

            if (diskConfig != null) {
                fallbackBundles.put(lowerLang, diskConfig);
            }
        }
    }

    @NotNull
    public static String getEffectiveLanguageCode(@Nullable CommandSender sender) {
        if (sender instanceof Player player) {
            if (isClarityLangCorePresent) {
                try {
                    String code = ClarityLangAPI.getPlayerLanguageCode(player);
                    if (code != null && !code.isBlank()) return code;
                } catch (Throwable ignored) {}
            }
            try {
                Locale clientLocale = player.locale();
                String lang = clientLocale.getLanguage().toLowerCase(Locale.ROOT);
                for (String supported : SUPPORTED_LANGS) {
                    if (supported.toLowerCase(Locale.ROOT).startsWith(lang)) return supported;
                }
            } catch (Throwable ignored) {}
        }
        return "en_US";
    }

    public static void sendMessage(@Nullable CommandSender sender, @NotNull String key, @NotNull TagResolver... resolvers) {
        if (sender == null) return;
        sender.sendMessage(getComponent(key, sender, resolvers));
    }

    public static void sendMessage(@Nullable Player player, @NotNull String key, @NotNull TagResolver... resolvers) {
        if (player == null) return;
        player.sendMessage(getComponent(key, player, resolvers));
    }

    @NotNull
    public static Component getComponent(@NotNull String key, @NotNull TagResolver... resolvers) {
        return getComponent(key, (CommandSender) null, resolvers);
    }

    @NotNull
    public static Component getComponent(@NotNull String key, @Nullable CommandSender sender, @NotNull TagResolver... resolvers) {
        String langCode = getEffectiveLanguageCode(sender);
        TagResolver[] enriched = buildResolversWithPrefix(langCode, resolvers);
        String cleanKey = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
        String fullKey = key.contains(":") ? key : namespace + ":" + key;

        Player player = sender instanceof Player p ? p : null;

        if (isClarityLangCorePresent) {
            Component comp = tryClarityLangCore(fullKey, cleanKey, player, langCode, enriched);
            if (comp != null) return comp;
        }
        return getFallbackComponent(cleanKey, langCode, enriched);
    }

    @Nullable
    private static Component tryClarityLangCore(String fullKey, String cleanKey, @Nullable Player player, String langCode, TagResolver[] resolvers) {
        try {
            Component comp = player != null
                    ? ClarityLangAPI.getComponent(fullKey, player, resolvers)
                    : ClarityLangAPI.getComponent(fullKey, langCode, resolvers);

            if (comp instanceof TextComponent tc) {
                String content = tc.content();
                if (content.equals("<" + fullKey + ">") || content.equals("<" + cleanKey + ">")
                        || content.equals(fullKey) || content.equals(cleanKey)) {
                    return null;
                }
            }
            return comp;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @NotNull
    public static String getString(@NotNull String key, @Nullable CommandSender sender) {
        String langCode = getEffectiveLanguageCode(sender);
        String raw = getRawString(key, langCode);
        return raw != null ? raw : key;
    }

    @Nullable
    public static String getRawString(@NotNull String key, @NotNull String langCode) {
        String cleanKey = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
        String fullKey = key.contains(":") ? key : namespace + ":" + key;

        if (isClarityLangCorePresent) {
            try {
                String val = ClarityLangAPI.getRawString(fullKey, langCode);
                if (val != null && !val.equals("<" + fullKey + ">") && !val.equals("<" + cleanKey + ">")
                        && !val.equals(fullKey) && !val.equals(cleanKey)) {
                    return val;
                }
            } catch (Throwable ignored) {}
        }
        return resolveRawKey(cleanKey, langCode);
    }

    @NotNull
    private static Component getFallbackComponent(@NotNull String cleanKey, @NotNull String langCode, @NotNull TagResolver... resolvers) {
        String raw = resolveRawKey(cleanKey, langCode);
        if (raw == null) raw = cleanKey;
        return MINI_MESSAGE.deserialize(raw, resolvers);
    }

    @Nullable
    private static String resolveRawKey(@NotNull String cleanKey, @NotNull String langCode) {
        String lowerLang = langCode.toLowerCase(Locale.ROOT);

        // 1. Disk fallback bundle for requested lang
        String raw = getRawFromConfig(fallbackBundles.get(lowerLang), cleanKey);
        if (raw != null) return raw;

        // 2. In-memory jar bundle for requested lang
        raw = getRawFromConfig(jarBundles.get(lowerLang), cleanKey);
        if (raw != null) return raw;

        // 3. Disk fallback bundle for en_US
        raw = getRawFromConfig(fallbackBundles.get("en_us"), cleanKey);
        if (raw != null) return raw;

        // 4. In-memory jar bundle for en_US
        return getRawFromConfig(jarBundles.get("en_us"), cleanKey);
    }

    @Nullable
    private static String getRawFromConfig(@Nullable YamlConfiguration config, @NotNull String cleanKey) {
        if (config == null) return null;
        return config.getString(cleanKey);
    }

    @NotNull
    private static TagResolver[] buildResolversWithPrefix(String langCode, @NotNull TagResolver... resolvers) {
        String prefix = resolveRawKey("prefix", langCode);
        if (prefix == null) prefix = "";
        List<TagResolver> list = new ArrayList<>(resolvers.length + 1);
        list.add(Placeholder.parsed("prefix", prefix));
        Collections.addAll(list, resolvers);
        return list.toArray(TagResolver[]::new);
    }

    public static boolean isClarityLangCorePresent() {
        return isClarityLangCorePresent;
    }
}
