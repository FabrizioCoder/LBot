package me.fabriziocoder.luxanna;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.merakianalytics.orianna.Orianna;
import me.fabriziocoder.luxanna.commands.core.PingCommand;
import me.fabriziocoder.luxanna.commands.lol.LoLCommand;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import no.stelar7.api.r4j.basic.APICredentials;
import no.stelar7.api.r4j.impl.R4J;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Luxanna {

    public static void main(String[] args) throws LoginException, IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/main/bot.properties"));


        CommandClientBuilder builder = new CommandClientBuilder()
                .setOwnerId(properties.getProperty("OWNER_ID"))
                .setCoOwnerIds(properties.getProperty("CO_OWNER_1_ID"), properties.getProperty("CO_OWNER_2_ID"))
                .setActivity(Activity.competing("a Ranked"))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .useHelpBuilder(false);

        builder.addSlashCommands(new PingCommand(), new LoLCommand());
        CommandClient commandClient = builder.build();

        JDABuilder.createLight(properties.getProperty("BOT_TOKEN"), GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(commandClient)
                .build();

        Orianna.setRiotAPIKey(properties.getProperty("RIOT_API_KEY"));
        Orianna.setDefaultLocale(properties.getProperty("DEFAULT_LOCALE"));
        new R4J(new APICredentials(properties.getProperty("RIOT_API_KEY")));
    }

}
