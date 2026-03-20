package io.github.jackmo03.warpdrive.compat.cct.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PShipCore implements IPeripheral {
    private final BlockPos pos;
    private final Level world;

    public PShipCore(Level world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    @Nonnull
    @Override
    public String getType() {
        return "warpdrive_shipcore";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return this == other || (other instanceof PShipCore && ((PShipCore) other).pos.equals(pos));
    }

    private LoadedServerShip getShip() {
        if (world instanceof ServerLevel) {
            return VSGameUtilsKt.getLoadedShipManagingPos((ServerLevel) world, pos);
        }
        return null;
    }

    @LuaFunction
    public final String getShipName() {
        LoadedServerShip ship = getShip();
        if (ship == null)
            return "No Ship";
        return ship.getSlug() != null ? ship.getSlug() : "Unnamed Ship";
    }

    @LuaFunction
    public final Long getId() {
        LoadedServerShip ship = getShip();
        return ship != null ? ship.getId() : null;
    }

    @LuaFunction
    public final Double getMass() {
        LoadedServerShip ship = getShip();
        return ship != null ? ship.getInertiaData().getMass() : null;
    }

    @LuaFunction
    public final Map<String, Double> getVelocity() {
        LoadedServerShip ship = getShip();
        if (ship == null)
            return null;
        Vector3dc vel = ship.getVelocity();
        Map<String, Double> map = new HashMap<>();
        map.put("x", vel.x());
        map.put("y", vel.y());
        map.put("z", vel.z());
        return map;
    }

    @LuaFunction
    public final Map<String, Object> getShipStatus() {
        Map<String, Object> status = new HashMap<>();
        LoadedServerShip ship = getShip();
        if (ship != null) {
            status.put("online", true);
            status.put("mass", ship.getInertiaData().getMass());
        } else {
            status.put("online", false);
        }
        return status;
    }

    @LuaFunction
    public final void applyBodyForce(double x, double y, double z) {
        LoadedServerShip ship = getShip();
        if (ship != null && world instanceof ServerLevel) {
            String dimId = VSGameUtilsKt.getDimensionId(world);
            ValkyrienSkiesMod.getOrCreateGTPA(dimId).applyBodyForce(ship.getId(), new Vector3d(x, y, z),
                    new Vector3d());
        }
    }

    @LuaFunction
    public final void applyWorldForce(double x, double y, double z) {
        LoadedServerShip ship = getShip();
        if (ship != null && world instanceof ServerLevel) {
            String dimId = VSGameUtilsKt.getDimensionId(world);
            ValkyrienSkiesMod.getOrCreateGTPA(dimId).applyWorldForce(ship.getId(), new Vector3d(x, y, z),
                    null);
        }
    }

    @LuaFunction
    public final void applyWorldTorque(double x, double y, double z) {
        LoadedServerShip ship = getShip();
        if (ship != null && world instanceof ServerLevel) {
            String dimId = VSGameUtilsKt.getDimensionId(world);
            ValkyrienSkiesMod.getOrCreateGTPA(dimId).applyWorldTorque(ship.getId(), new Vector3d(x, y, z));
        }
    }

    @LuaFunction
    public final void applyBodyTorque(double x, double y, double z) {
        LoadedServerShip ship = getShip();
        if (ship != null && world instanceof ServerLevel) {
            String dimId = VSGameUtilsKt.getDimensionId(world);
            ValkyrienSkiesMod.getOrCreateGTPA(dimId).applyBodyTorque(ship.getId(), new Vector3d(x, y, z));
        }
    }
}