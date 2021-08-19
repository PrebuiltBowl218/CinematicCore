package me.aleiv.core.paper.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.taskchain.TaskChain;
import lombok.NonNull;
import me.aleiv.core.paper.Core;
import me.aleiv.core.paper.objects.Cinematic;
import me.aleiv.core.paper.objects.Frame;
import net.md_5.bungee.api.ChatColor;

@CommandAlias("cinematic")
@CommandPermission("cinematic.cmd")
public class CinematicCMD extends BaseCommand {

    private @NonNull Core instance;

    public CinematicCMD(Core instance) {
        this.instance = instance;

    }

    public void record(Player player, List<Frame> frames, int seconds){
        var chain = Core.newChain();

        var count = 0;
        for (int i = 0; i < seconds; i++) {
            for (int j = 0; j < 20; j++) {

                var c = (int) count/20;
                
                chain.delay(1).sync(() -> {
                    player.sendActionBar(ChatColor.YELLOW + "" + c + "/" + seconds);
                    var loc = player.getLocation().clone();
                    var frame = new Frame(loc.getWorld().getName().toString(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    frames.add(frame);

                });
                count++;
            }
            
        }

        chain.sync(TaskChain::abort).execute();
    }

    @Subcommand("rec")
    public void rec(Player sender, String cinematic, int seconds){

        var game = instance.getGame();
        var cinematics = game.getCinematics();
        

        if(cinematics.containsKey(cinematic)){
            sender.sendMessage(ChatColor.RED + "Cinematic already exist.");

        }else{


            var cine = new Cinematic(cinematic);
            cinematics.put(cinematic, cine);
            
            var frames = cine.getFrames();
            
            var chain = Core.newChain();

            var count = 3;
            while(count >= 0){
                final var c = count;
                chain.delay(20).sync(() -> {

                    if(c == 0){
                        sender.sendTitle(ChatColor.DARK_RED + "REC.", "", 0, 20, 20);
                        sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        record(sender, frames, seconds);

                    }else{
                        sender.sendTitle(ChatColor.DARK_RED + "" + c, "", 0, 20, 20);
                        sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1, 1);
                    }
                    
                });
                count--;
            }
    
            chain.sync(TaskChain::abort).execute();

        }

    }

    @Subcommand("play")
    @CommandCompletion("@players")
    public void play(CommandSender sender, @Flags("other") Player player, String cinematic){

        var game = instance.getGame();
        var cinematics = game.getCinematics();

        if(!cinematics.containsKey(cinematic)){
            sender.sendMessage(ChatColor.RED + "Cinematic doesn't exist.");

        }else{
            var cine = cinematics.get(cinematic);
            var frames = cine.getFrames();

            var chain = Core.newChain();

            frames.forEach(frame ->{
                chain.delay(1).sync(() -> {
                    var world = Bukkit.getWorld(frame.getWorld());
                    var loc = new Location(world, frame.getX(), frame.getY(), frame.getZ(), frame.getYaw(), frame.getPitch());
                    player.teleport(loc);
                });
            });
    
            chain.sync(TaskChain::abort).execute();
        
        }

    }
}
