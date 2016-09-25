package xyz.saboteur.spigot.util.cmd;

import com.sk89q.intake.argument.Namespace;
import com.sk89q.intake.util.auth.Authorizer;
import org.bukkit.entity.Player;
import static com.google.common.base.Preconditions.checkNotNull;

public class PlayerAuthorizer implements Authorizer {
    @Override
    public boolean testPermission(Namespace namespace, String permission) {
        return checkNotNull((Player)namespace.get("sender"), "Current user not available").hasPermission(permission);
    }
}
