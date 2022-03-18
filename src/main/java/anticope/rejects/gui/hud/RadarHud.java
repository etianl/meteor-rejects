package anticope.rejects.gui.hud;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.modules.HudElement;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.misc.Vec3;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class RadarHud extends HudElement {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> backgroundColor = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color of background.")
        .defaultValue(new SettingColor(0, 0, 0, 64))
        .build()
    );


    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Select specific entities.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<Boolean> letters = sgGeneral.add(new BoolSetting.Builder()
            .name("letters")
            .description("Use entity's type first letter.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> showWaypoints = sgGeneral.add(new BoolSetting.Builder()
            .name("waypoints")
            .description("Show waypoints.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 5)
        .build()
    );

    public RadarHud(HUD hud) {
        super(hud, "Radar", "Draws a Radar on your HUD telling you where entities are");
    }

    @Override
    public void update(HudRenderer renderer) {
        box.setSize(100 * scale.get(), 100 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        ESP esp = Modules.get().get(ESP.class);
        Waypoints waypoints = Waypoints.get();
        if (esp == null) return;
        renderer.addPostTask(() -> {
            double x = box.getX();
            double y = box.getY();

            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, box.width, box.height, backgroundColor.get());
            Renderer2D.COLOR.render(null);
            if (mc.world != null) {
                for (Entity entity : mc.world.getEntities()) {
                    if (entity.getPos().distanceTo(mc.player.getPos()) > 100) continue;
                    if (!entities.get().getBoolean(entity.getType())) return;
                    double xPos = ((entity.getX() - mc.player.getX()) * scale.get() + box.width) / 2 + x;
                    double yPos = ((entity.getZ() - mc.player.getZ()) * scale.get() + box.height) / 2 + y;
                    String icon = "*";
                    if (letters.get()) 
                        icon = entity.getType().getUntranslatedName().substring(0,1).toUpperCase();
                    renderer.text(icon, xPos, yPos, esp.getColor(entity));
                }
            }
            if (showWaypoints.get()) {
                for (Waypoint waypoint : waypoints.waypoints) {
                    Vec3 c = waypoint.getCoords();
                    Vec3d coords = new Vec3d(c.x, c.y, c.z);
                    if (coords.distanceTo(mc.player.getPos()) > 100) continue;
                    double xPos = ((coords.getX() - mc.player.getX()) * scale.get() + box.width) / 2 + x;
                    double yPos = ((coords.getZ() - mc.player.getZ()) * scale.get() + box.height) / 2 + y;
                    String icon = "*";
                    if (letters.get() && waypoint.name.length() > 0 && waypoint.visible)
                        icon = waypoint.name.substring(0, 1);
                    renderer.text(icon, xPos, yPos, waypoint.color);
                }
            }
            Renderer2D.COLOR.render(null);
        });
        
    }
    
}