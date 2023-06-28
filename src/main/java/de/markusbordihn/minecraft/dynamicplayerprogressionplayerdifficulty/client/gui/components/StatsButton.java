/**
 * Copyright 2022 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.minecraft.dynamicplayerprogressionplayerdifficulty.client.gui.components;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import de.markusbordihn.minecraft.dynamicplayerprogressionplayerdifficulty.Constants;
import de.markusbordihn.minecraft.dynamicplayerprogressionplayerdifficulty.data.PlayerData;
import de.markusbordihn.minecraft.dynamicplayerprogressionplayerdifficulty.data.PlayerDataManager;
import de.markusbordihn.minecraft.dynamicplayerprogressionplayerdifficulty.data.WeaponClass;
import de.markusbordihn.minecraft.dynamicplayerprogressionplayerdifficulty.data.WeaponClassData;

public class StatsButton extends Button {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final Component EMPTY_TEXT = Component.literal("");
  private final Minecraft minecraft;
  private final Font font;

  private static final ResourceLocation TEXTURE =
      new ResourceLocation(Constants.MOD_ID, "textures/gui/texture.png");
  public static final ResourceLocation TOOLTIP_TEXTURE =
      new ResourceLocation("textures/gui/container/bundle.png");

  public StatsButton(int x, int y, int width, int height, OnPress onPress) {
    this(x, y, width, height, onPress, Button.DEFAULT_NARRATION);
  }

  public StatsButton(int x, int y, int width, int height, OnPress onPress,
      CreateNarration createNarration) {
    super(x, y, width, height, EMPTY_TEXT, onPress, createNarration);
    this.minecraft = Minecraft.getInstance();
    this.font = this.minecraft.font;
  }

  public void renderStatsOverview(PoseStack poseStack, int mouseX, int mouseY) {
    int x = mouseX + 5;
    int y = mouseY - 50;
    int spacingSecondColum = 104;
    int width = 12;
    int height = 12;

    // Player Data
    PlayerData playerData = PlayerDataManager.getLocalPlayer();
    if (playerData == null) {
      return;
    }

    // Background
    poseStack.pushPose();
    poseStack.translate(0, 0, 1000);
    GuiComponent.fillGradient(poseStack, x + 1, y + 1, (x + width * 18) + 1, (y + height * 20),
        -1072689136, -804253680);
    poseStack.popPose();

    // Border
    poseStack.pushPose();
    poseStack.translate(0, 0, 100);
    this.drawBorder(x, y, width, height, poseStack);
    poseStack.popPose();

    // Player stats with 4 padding.
    poseStack.pushPose();
    poseStack.translate(0, 0, 1000);
    y += 4;
    x += 4;

    // General Stats
    y = drawStats(poseStack, x, y,
        Component.translatable(Constants.STATS_TEXT_PREFIX, playerData.getUsername()));
    y = drawStats(poseStack, x, y, Component.translatable(Constants.STATS_TEXT_PREFIX + "deaths",
        playerData.getNumberOfDeaths()));
    y = drawSeparator(poseStack, x, y, width);

    // Penalty Stats
    y = drawStats(poseStack, x, y,
        Component.translatable(Constants.STATS_TEXT_PREFIX + "experience_penalty"));
    y = drawStats(poseStack, x, y, Component.translatable(Constants.STATS_TEXT_PREFIX + "penalty",
        playerData.getExperiencePenaltyGeneral(), playerData.getExperiencePenaltyWeaponClass()));
    y = drawSeparator(poseStack, x, y, width);

    // Damage Stats
    y = drawStats(poseStack, x, y,
        Component.translatable(Constants.STATS_TEXT_PREFIX + "damage_stats"));
    y = drawStats(poseStack, x, y,
        Component.translatable(Constants.STATS_TEXT_PREFIX + "mob_damage", playerData.getMobKills(),
            playerData.getDamageLevelMob()));
    if (playerData.getPvPEnabled()) {
      y = drawStats(poseStack, x, y,
          Component.translatable(Constants.STATS_TEXT_PREFIX + "player_damage",
              playerData.getPlayerKills(), playerData.getDamageLevelPlayer()));
    }
    y = drawSeparator(poseStack, x, y, width);

    // Weapon Stats
    y = drawStats(poseStack, x, y,
        Component.translatable(Constants.STATS_TEXT_PREFIX + "weapon_stats"));
    boolean evenTextPlacement = true;
    for (WeaponClass weaponClass : WeaponClass.values()) {
      if (WeaponClassData.isWeaponClassEnabled(weaponClass) || Constants.IS_MOD_DEV) {
        if (evenTextPlacement) {
          drawStats(poseStack, x, y,
              Component.translatable(Constants.LEVEL_TEXT_PREFIX, weaponClass.textIcon,
                  weaponClass.text.withStyle(ChatFormatting.RESET),
                  playerData.getWeaponClassLevel(weaponClass)));
        } else {
          y = drawStats(poseStack, x + spacingSecondColum, y,
              Component.translatable(Constants.LEVEL_TEXT_PREFIX, weaponClass.textIcon,
                  weaponClass.text.withStyle(ChatFormatting.RESET),
                  playerData.getWeaponClassLevel(weaponClass)));
        }
        evenTextPlacement = !evenTextPlacement;
      }
    }
    poseStack.popPose();
  }

  private int drawSeparator(PoseStack poseStack, int x, int y, int width) {
    for (int i = 0; i < width; ++i) {
      this.blit(poseStack, x - 3 + i * 18, y, Texture.BORDER_HORIZONTAL_TOP);
    }
    return y + 4;
  }

  private int drawStats(PoseStack poseStack, int x, int y, Component component) {
    this.font.draw(poseStack, component, x, y, 0xff0000);
    return y + this.font.lineHeight + 1;
  }

  private void drawBorder(int x, int y, int width, int height, PoseStack poseStack) {
    this.blit(poseStack, x, y, Texture.BORDER_CORNER_TOP);
    this.blit(poseStack, x + width * 18 + 1, y, Texture.BORDER_CORNER_TOP);
    for (int i = 0; i < width; ++i) {
      this.blit(poseStack, x + 1 + i * 18, y, Texture.BORDER_HORIZONTAL_TOP);
      this.blit(poseStack, x + 1 + i * 18, y + height * 20, Texture.BORDER_HORIZONTAL_BOTTOM);
    }
    for (int j = 0; j < height; ++j) {
      this.blit(poseStack, x, y + j * 20 + 1, Texture.BORDER_VERTICAL);
      this.blit(poseStack, x + width * 18 + 1, y + j * 20 + 1, Texture.BORDER_VERTICAL);
    }
    this.blit(poseStack, x, y + height * 20, Texture.BORDER_CORNER_BOTTOM);
    this.blit(poseStack, x + width * 18 + 1, y + height * 20, Texture.BORDER_CORNER_BOTTOM);
  }

  private void blit(PoseStack poseStack, int x, int y, Texture texture) {
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, TOOLTIP_TEXTURE);
    GuiComponent.blit(poseStack, x, y, 0, texture.x, texture.y, texture.w, texture.h, 128, 128);
  }

  @Override
  public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderTexture(0, TEXTURE);
    if (isHoveredOrFocused()) {
      blit(poseStack, this.getX(), this.getY(), 34, 1, 10, 10);
      this.renderStatsOverview(poseStack, mouseX, mouseY);
    } else {
      blit(poseStack, this.getX(), this.getY(), 24, 1, 10, 10);
    }
  }

  @OnlyIn(Dist.CLIENT)
  enum Texture {
    //@formatter:off
    BORDER_VERTICAL(0, 18, 1, 20),
    BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
    BORDER_HORIZONTAL_BOTTOM(0,60, 18, 1),
    BORDER_CORNER_TOP(0, 20, 1, 1),
    BORDER_CORNER_BOTTOM(0, 60, 1, 1);
    //@formatter:on

    public final int x;
    public final int y;
    public final int w;
    public final int h;

    private Texture(int x, int y, int w, int h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }
  }

}
