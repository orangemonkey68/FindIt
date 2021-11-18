package me.s0vi.findit.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.s0vi.findit.FindItConfig;
import me.shedaniel.autoconfig.AutoConfig;

public class FindItModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            return AutoConfig.getConfigScreen(FindItConfig.class, parent).get();
        };
    }
}
