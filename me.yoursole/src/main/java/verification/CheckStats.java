package verification;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.hypixel.api.HypixelAPI;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import java.nio.channels.Channel;
import java.text.ParseException;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class CheckStats extends ListenerAdapter{
    public static String meetsreqs;
    public static boolean req = false;
    public void onGuildMessageReceived(GuildMessageReceivedEvent e){
        Message textraw = e.getMessage();
        TextChannel channel = e.getChannel();
        String Message1 = e.getMessage().getContentRaw();
        String[] args = Message1.split(" ", 2);
        String IGN = null;
        if(args.length==2){
             IGN = args[1];
        }
        String UUID = null;
        char[] withQ = Message1.toCharArray();
        if(withQ[0]=='?' && args.length==2){
            try {
                UUID = getUuid(IGN , e.getChannel());
            } catch (IOException | ExecutionException ioException) {
                ioException.printStackTrace();
            }
        }
        try {
            IGN = GetRealName(UUID);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        if(withQ[0]=='?'){
            try {
                apply(args[0], channel, textraw, UUID, IGN);
            } catch (ExecutionException | NullPointerException executionException) {
                executionException.printStackTrace();
            }
        }

    }
    public String getUuid(String name, TextChannel channel) throws IOException, ExecutionException {
        String uuid = null;
        String url = "https://api.mojang.com/users/profiles/minecraft/"+name;
        String UUIDJson = null;
        try{
            UUIDJson = new Scanner(new URL(url).openStream(),"UTF-8").useDelimiter("\\A").next();
        }catch (net.hypixel.api.exceptions.HypixelAPIException e){
            channel.sendMessage("That is not a valid Username").queue();
        }
        JsonObject JsonFile = (JsonObject) new JsonParser().parse(UUIDJson);
        uuid=JsonFile.get("id").toString();
        uuid = removeFirstandLast(uuid);
        return uuid;
    }
    public String GetRealName(String UUID) throws IOException{
        String name = null;
        String url = "https://api.mojang.com/user/profiles/"+UUID+"/names";
        String UUIDJson = null;
        UUIDJson = new Scanner(new URL(url).openStream(),"UTF-8").useDelimiter("\\A").next();
        JsonArray JsonFile = (JsonArray) new JsonParser().parse(UUIDJson);
        name =JsonFile.get(JsonFile.size()-1).getAsJsonObject().get("name").toString();
        name = name.replaceAll("\"","");
        return name;
    }
    public String removeFirstandLast(String str) {
        str = str.substring(1, str.length() - 1);
        return str;
    }
    public static void apply(String command, TextChannel channel, Message text, String uuid, String ign) throws ExecutionException {
        if(command.equalsIgnoreCase("?reqs")){
            final UUID API_UUID = UUID.fromString("cf5c2051-35d6-4d1a-88f9-57924b6ed9a4");
            HypixelAPI api = new HypixelAPI(API_UUID);
            JsonObject json = null;
            try {
                json = new JsonParser().parse(api.getPlayerByUuid(uuid).get().getPlayer().toString()).getAsJsonObject();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int secReqs = 0;
            int coreReqs = 0;
            //network lvl
            int networklvl = 0;
            if(json.get("networkExp")!=null){
                int networkExp = json.get("networkExp").getAsInt();
                int networkLvl = (int) ((int)Math.sqrt(networkExp/1250 + 12.25) - 2.5);
                networklvl=networkLvl;
            }
            //AP
            //Quests
            //---------------------------------------
            assert json != null;
            if(json.get("achievements")!=null){
                JsonObject achievements = json.getAsJsonObject("achievements");
                if(achievements.get("arcade_arcade_winner")!=null&&achievements.get("arcade_arcade_winner").getAsInt()>=gameRequirements.arcadeWinsLOW){
                    if(achievements.get("arcade_arcade_winner").getAsInt()>=gameRequirements.arcadeWinsHIGH){ ;
                        secReqs+=3;
                    }else{
                        secReqs+=1;
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("Arena")!=null){
                    JsonObject Arena = stats.getAsJsonObject("Arena");
                    if(Arena.get("wins")!=null&&Arena.get("kills_1v1")!=null&&Arena.get("kills_2v2")!=null&&Arena.get("kills_4v4")!=null){
                        if(Arena.get("wins").getAsInt()>gameRequirements.ArenaWinsLOW&&
                                Arena.get("kills_1v1").getAsInt()+Arena.get("kills_2v2").getAsInt()+Arena.get("kills_4v4").getAsInt()>gameRequirements.ArenaKillsLOW){
                            if(Arena.get("wins").getAsInt()>gameRequirements.ArenaWinsHIGH&&
                                    Arena.get("kills_1v1").getAsInt()+Arena.get("kills_2v2").getAsInt()+Arena.get("kills_4v4").getAsInt()>gameRequirements.ArenaKillsHIGH){
                                secReqs+=3;
                            }else{
                                secReqs+=1;
                            }
                        }
                    }
                }
            }
            if(json.get("achievements")!=null&&json.get("stats")!=null){
                JsonObject achievements = json.getAsJsonObject("achievements");
                JsonObject stats = json.getAsJsonObject("stats");
                if(achievements.get("bedwars_level")!=null&&stats.get("Bedwars")!=null){
                    JsonObject bedwars = stats.getAsJsonObject("Bedwars");
                    if(bedwars.get("wins_bedwars")!=null&&bedwars.get("wins_bedwars").getAsInt()>=gameRequirements.BedwarsWinsLOW&&
                            achievements.get("bedwars_level").getAsInt()>=gameRequirements.BedwarsLevelLOW){
                        if(bedwars.get("wins_bedwars").getAsInt()>=gameRequirements.BedwarsWinsHIGH&&
                                achievements.get("bedwars_level").getAsInt()>=gameRequirements.BedwarsLevelHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("HungerGames")!=null){
                    JsonObject BSG = stats.getAsJsonObject("HungerGames");
                    if(BSG.get("kills")!=null&&BSG.get("wins")!=null&&
                            BSG.get("kills").getAsInt()>=gameRequirements.BSGkillsLOW&&BSG.get("wins").getAsInt()>=gameRequirements.BSGwinsLOW){
                        if(BSG.get("kills").getAsInt()>=gameRequirements.BSGkillsHIGH&&BSG.get("wins").getAsInt()>=gameRequirements.BSGwinsHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("BuildBattle")!=null){
                    JsonObject BB = stats.getAsJsonObject("BuildBattle");
                    if(BB.get("score")!=null&&BB.get("score").getAsInt()>=gameRequirements.BBscoreLOW){
                        if(BB.get("score").getAsInt()>=gameRequirements.BBscoreHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("MCGO")!=null){
                    JsonObject MCGO = stats.getAsJsonObject("MCGO");
                    if(MCGO.get("cop_kills")!=null&&MCGO.get("cop_kills_deathmatch")!=null&&MCGO.get("criminal_kills")!=null&&
                            MCGO.get("criminal_kills_deathmatch")!=null&&MCGO.get("game_wins")!=null&&MCGO.get("cop_kills").getAsInt()+
                            MCGO.get("cop_kills_deathmatch").getAsInt()+MCGO.get("criminal_kills").getAsInt()+MCGO.get("criminal_kills_deathmatch").getAsInt()
                            >=gameRequirements.CVCkillsLOW&&MCGO.get("game_wins").getAsInt()>=gameRequirements.CVCwinsLOW){
                        if(MCGO.get("cop_kills").getAsInt()+
                                MCGO.get("cop_kills_deathmatch").getAsInt()+MCGO.get("criminal_kills").getAsInt()+MCGO.get("criminal_kills_deathmatch").getAsInt()
                                >=gameRequirements.CVCkillsHIGH&&MCGO.get("game_wins").getAsInt()>=gameRequirements.CVCwinsHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("Duels")!=null){
                    JsonObject duels = stats.getAsJsonObject("Duels");
                    if(duels.get("wins")!=null&&duels.get("wins").getAsInt()>=gameRequirements.duelsWinsLOW){
                        if(duels.get("wins").getAsInt()>=gameRequirements.duelsWinsHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("Walls3")!=null){
                    JsonObject MegaWalls = stats.getAsJsonObject("Walls3");
                    if(MegaWalls.get("final_kills")!=null&&MegaWalls.get("wins")!=null&&
                            MegaWalls.get("final_kills").getAsInt()>=gameRequirements.MWFinalsLOW&&
                            MegaWalls.get("wins").getAsInt()>=gameRequirements.MWwinsLOW){
                        if(MegaWalls.get("final_kills").getAsInt()>=gameRequirements.MWFinalsHIGH&&
                                MegaWalls.get("wins").getAsInt()>=gameRequirements.MWwinsHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("MurderMystery")!=null){
                    JsonObject MM = stats.getAsJsonObject("MurderMystery");
                    if(MM.get("wins")!=null&&MM.get("murderer_wins")!=null&&
                            MM.get("wins").getAsInt()>=gameRequirements.MysteryWinsLOW&&
                            MM.get("murderer_wins").getAsInt()>=gameRequirements.MurderWinsLOW){
                        if(MM.get("wins").getAsInt()>=gameRequirements.MysteryWinsHIGH&&
                                MM.get("murderer_wins").getAsInt()>=gameRequirements.MurderWinsHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("Paintball")!=null){
                    JsonObject paintball = stats.getAsJsonObject("Paintball");
                    if(paintball.get("kills")!=null&&paintball.get("kills").getAsInt()>=gameRequirements.paintballKillsLOW){
                        if(paintball.get("kills").getAsInt()>=gameRequirements.paintballKillsHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("Quake")!=null){
                    JsonObject quake = stats.getAsJsonObject("Quake");
                    if(quake.get("kills")!=null&&quake.get("kills_teams")!=null&&
                        quake.get("kills").getAsInt()+quake.get("kills_teams").getAsInt()>=
                        gameRequirements.QuakeKillsLOW){
                        if(quake.get("kills").getAsInt()+quake.get("kills_teams").getAsInt()>=
                                gameRequirements.QuakeKillsHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("achievements")!=null){
                JsonObject stats = json.getAsJsonObject("achievements");
                if(stats.get("skywars_you_re_a_star")!=null&&stats.get("skywars_you_re_a_star").getAsInt()>=gameRequirements.SWstarLOW){
                    if(stats.get("skywars_you_re_a_star").getAsInt()>=gameRequirements.SWstarHIGH){
                        secReqs+=3;
                    }else{
                        secReqs+=1;
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("SuperSmash")!=null){
                    JsonObject smash = stats.getAsJsonObject("SuperSmash");
                    if(smash.get("smashLevel")!=null&&smash.get("kills")!=null&&
                        smash.get("smashLevel").getAsInt()>=gameRequirements.SmashLevelLOW&&
                        smash.get("kills").getAsInt()>=gameRequirements.SmashKillsLOW){
                        if(smash.get("smashLevel").getAsInt()>=gameRequirements.SmashLevelHIGH&&
                                smash.get("kills").getAsInt()>=gameRequirements.SmashKillsHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("SpeedUHC")!=null){
                    JsonObject SpeedUHC = stats.getAsJsonObject("SpeedUHC");
                    if(SpeedUHC.get("score")!=null&&SpeedUHC.get("score").getAsInt()>=gameRequirements.SpeedUHCscoreLOW){
                        if(SpeedUHC.get("score").getAsInt()>=gameRequirements.SpeedUHCscoreHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("Pit")!=null){
                    JsonObject pit = stats.getAsJsonObject("Pit");
                    if(pit.get("profile")!=null){
                        JsonObject profile = pit.getAsJsonObject("profile");
                        if(profile.get("prestiges")!=null){
                            JsonArray prestiges = profile.getAsJsonArray("prestiges");
                            if(prestiges.size()+1>=gameRequirements.PitPresLOW){
                                if(prestiges.size()+1>=gameRequirements.PitPresHIGH){
                                    secReqs+=3;
                                }else{
                                    secReqs+=1;
                                }
                            }
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("TNTGames")!=null){
                    JsonObject TNTGames = stats.getAsJsonObject("TNTGames");
                    if(TNTGames.get("wins")!=null&&TNTGames.get("wins").getAsInt()>=gameRequirements.TNTgamesWinsLOW){
                        if(TNTGames.get("wins").getAsInt()>=gameRequirements.TNTgamesWinsHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("GingerBread")!=null){
                    JsonObject TKR = stats.getAsJsonObject("GingerBread");
                    if(TKR.get("gold_trophy")!=null&&TKR.get("laps_completed")!=null&&
                        TKR.get("gold_trophy").getAsInt()>=gameRequirements.TKRgoldLOW&&
                        TKR.get("laps_completed").getAsInt()>=gameRequirements.TKRlapsLOW){
                        if(TKR.get("gold_trophy").getAsInt()>=gameRequirements.TKRgoldHIGH&&
                                TKR.get("laps_completed").getAsInt()>=gameRequirements.TKRlapsHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("UHC")!=null){
                    JsonObject UHC = stats.getAsJsonObject("UHC");
                    if(UHC.get("score")!=null&&UHC.get("score").getAsInt()>=gameRequirements.UHCscoreLOW){
                        if(UHC.get("score").getAsInt()>=gameRequirements.UHCscoreHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("VampireZ")!=null){
                    JsonObject Vampz = stats.getAsJsonObject("VampireZ");
                    if(Vampz.get("human_wins")!=null&&Vampz.get("human_kills")!=null&&
                            Vampz.get("human_wins").getAsInt()>=gameRequirements.VampzWinsLOW&&
                            Vampz.get("human_kills").getAsInt()>=gameRequirements.VampzKillsLOW){
                        if(Vampz.get("human_wins").getAsInt()>=gameRequirements.VampzWinsHIGH&&
                                Vampz.get("human_kills").getAsInt()>=gameRequirements.VampzKillsHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("Walls")!=null){
                    JsonObject Walls = stats.getAsJsonObject("Walls");
                    if(Walls.get("wins")!=null&&Walls.get("kills")!=null&&
                        Walls.get("wins").getAsInt()>=gameRequirements.WallsWinsLOW&&
                        Walls.get("kills").getAsInt()>=gameRequirements.WallsKillsLOW){
                        if(Walls.get("wins").getAsInt()>=gameRequirements.WallsWinsHIGH&&
                                Walls.get("kills").getAsInt()>=gameRequirements.WallsKillsHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            if(json.get("stats")!=null){
                JsonObject stats = json.getAsJsonObject("stats");
                if(stats.get("Battleground")!=null){
                    JsonObject Warlords = stats.getAsJsonObject("Battleground");
                    if(Warlords.get("wins")!=null&&Warlords.get("kills")!=null&&
                        Warlords.get("wins").getAsInt()>=gameRequirements.WarlordsWinsLOW&&
                        Warlords.get("kills").getAsInt()>=gameRequirements.WarlordsKillsLOW){
                        if(Warlords.get("wins").getAsInt()>=gameRequirements.WarlordsWinsHIGH&&
                                Warlords.get("kills").getAsInt()>=gameRequirements.WarlordsKillsHIGH){
                            secReqs+=3;
                        }else{
                            secReqs+=1;
                        }
                    }
                }
            }
            //----------------------------
            //----------------------------
            //----------------------------
            //----------------------------
            //----------------------------
            channel.sendMessage(createEmbed(ign, networklvl, secReqs, uuid)).queue();
        }
        else{
            channel.sendMessage("That is not a command!").queue();
        }
    }

    public static MessageEmbed createEmbed(String IGN, int nwl, int points, String uuid){
        String metRequirements = null;
        boolean meetsCore = false;
        if(nwl >= 100){
            meetsCore=true;
        }
        if(meetsCore&&points<5){
            if(points==1){
                metRequirements = "Core Requirements, but only has "+points+" point";
            }else{
                metRequirements = "Core Requirements, but only has "+points+" points";
            }
        }else if(meetsCore&&points>=5){
            metRequirements = "Core Requirements, and has "+points+" points";
        }else if(!meetsCore){
            metRequirements = "no requirements";
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Statistic Check: "+IGN);
        embedBuilder.setDescription("["+IGN+"'s Plancke](https://plancke.io/hypixel/player/stats/"+IGN+")");
        embedBuilder.setThumbnail("https://crafatar.com/avatars/"+uuid+"?overlay");
        embedBuilder.addField("The Premise Stat Checker","This player meets "+metRequirements,false);
        embedBuilder.addField("Unimplemented Requirements","Achievement Points\nNetwork Level with Quests\n" +
                "Seasonal Games\nSkyBlock\nI am working on finishing these, and I would love help\nPlease DM me if you " +
                "want to help!",false);
        embedBuilder.setFooter("Created with love by Yoursole1#7254");
        embedBuilder.setColor(new Color(2871056));
        return embedBuilder.build();
    }
}
