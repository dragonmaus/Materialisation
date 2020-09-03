package me.shedaniel.materialisation.config;

import me.shedaniel.materialisation.api.BetterIngredient;
import me.shedaniel.materialisation.api.PartMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("ALL")
public class MaterialisationConfig {
    
    public static class ConfigIngredient {
        @SuppressWarnings("CanBeFinal")
        public BetterIngredient.Type type;
        @SuppressWarnings("CanBeFinal")
        public String content;
        @SuppressWarnings("CanBeFinal")
        public int count;
        
        public ConfigIngredient(BetterIngredient.Type type, String content) {
            this(type, content, 1);
        }
        
        public ConfigIngredient(BetterIngredient.Type type, String content, int count) {
            this.type = type;
            this.content = content;
            this.count = count;
        }
        
        public BetterIngredient toBetterIngredient() {
            return new BetterIngredient(type, content, count);
        }
    }
    
    public static class ConfigIngredients {
        @SuppressWarnings("CanBeFinal")
        public ConfigIngredient ingredient;
        @SuppressWarnings("CanBeFinal")
        public float multiplier;
        
        public ConfigIngredients(ConfigIngredient ingredient, float multiplier) {
            this.ingredient = ingredient;
            this.multiplier = multiplier;
        }
    }
    
    public static class ConfigMaterial implements PartMaterial {
        @SuppressWarnings("CanBeFinal")
        public boolean enabled = true;
        @SuppressWarnings("CanBeFinal")
        public String toolColor;
        // Will be rounded down
        @SuppressWarnings("CanBeFinal")
        public double toolDurability;
        // Will be rounded down
        @SuppressWarnings("CanBeFinal")
        public double miningLevel;
        // Will be rounded down
        @SuppressWarnings("CanBeFinal")
        public double enchantability;
        @SuppressWarnings("CanBeFinal")
        public double durabilityMultiplier;
        @SuppressWarnings("CanBeFinal")
        public double breakingSpeedMultiplier;
        @SuppressWarnings("CanBeFinal")
        public double toolSpeed;
        @SuppressWarnings("CanBeFinal")
        public double attackDamage;
        @SuppressWarnings("CanBeFinal")
        public String name;
        public String materialTranslationKey;
        @SuppressWarnings("CanBeFinal")
        public boolean bright;
        @SuppressWarnings("CanBeFinal")
        public List<ConfigIngredients> ingredients;
        // Will be rounded down
        @SuppressWarnings("CanBeFinal")
        public double fullAmount;
        private transient Integer color;
        private transient Map<BetterIngredient, Float> ingredientFloatMap = null;
        private transient Identifier identifierCache = null;
        
        public ConfigMaterial(PartMaterial partMaterial) {
            this.toolColor = partMaterial.getToolColor() + "";
            this.color = partMaterial.getToolColor();
            this.toolDurability = partMaterial.getToolDurability();
            this.miningLevel = partMaterial.getMiningLevel();
            this.enchantability = partMaterial.getEnchantability();
            this.durabilityMultiplier = partMaterial.getDurabilityMultiplier();
            this.breakingSpeedMultiplier = partMaterial.getBreakingSpeedMultiplier();
            this.toolSpeed = partMaterial.getToolSpeed();
            this.attackDamage = partMaterial.getAttackDamage();
            this.name = partMaterial.getIdentifier().toString();
            this.materialTranslationKey = partMaterial.getMaterialTranslateKey();
            this.bright = partMaterial.isBright();
            this.ingredients = ConfigHelper.fromMap(partMaterial.getIngredientMap());
            this.fullAmount = partMaterial.getFullAmount();
        }
        
        @Override
        public int getEnchantability() {
            return (int) enchantability;
        }
        
        @Override
        public int getToolColor() {
            if (color != null)
                return color;
            if (toolColor.startsWith("#")) {
                int a = Integer.valueOf(toolColor.substring(1, 3), 16);
                int r = Integer.valueOf(toolColor.substring(3, 5), 16);
                int g = Integer.valueOf(toolColor.substring(5, 7), 16);
                int b = Integer.valueOf(toolColor.substring(7, 9), 16);
                return color = ((a & 0xFF) << 24) |
                               ((r & 0xFF) << 16) |
                               ((g & 0xFF) << 8) |
                               ((b & 0xFF));
            }
            return color = Integer.decode(toolColor);
        }
        
        @Override
        public String getMaterialTranslateKey() {
            if (materialTranslationKey != null)
                return materialTranslationKey;
            return materialTranslationKey = "material.materialisation." + getIdentifier().toString().replace(':', '.');
        }
        
        @Override
        public Set<BetterIngredient> getIngredients() {
            return getIngredientMap().keySet();
        }
        
        @Override
        public Map<BetterIngredient, Float> getIngredientMap() {
            if (ingredientFloatMap != null)
                return ingredientFloatMap;
            return ingredientFloatMap = ConfigHelper.fromJson(ingredients);
        }
        
        @Override
        public Identifier getIdentifier() {
            if (identifierCache != null)
                return identifierCache;
            return identifierCache = new Identifier(name);
        }
        
        @Override
        public boolean isBright() {
            return bright;
        }
        
        @Override
        public double getDurabilityMultiplier() {
            return durabilityMultiplier;
        }
        
        @Override
        public double getBreakingSpeedMultiplier() {
            return breakingSpeedMultiplier;
        }
        
        @Override
        public double getAttackDamage() {
            return attackDamage;
        }
        
        @Override
        public int getToolDurability() {
            return (int) toolDurability;
        }
        
        @Override
        public double getToolSpeed() {
            return toolSpeed;
        }
        
        @Override
        public int getMiningLevel() {
            return (int) miningLevel;
        }
        
        @Override
        public int getFullAmount() {
            return (int) fullAmount;
        }
        
        @Override
        public float getRepairMultiplier(ItemStack stack) {
            BetterIngredient ingredient = null;
            for (BetterIngredient ingredient1 : getIngredients()) {
                if (ingredient1.isIncluded(stack)) {
                    ingredient = ingredient1;
                    break;
                }
            }
            if (ingredient != null) {
                BetterIngredient finalIngredient = ingredient;
                Optional<Map.Entry<BetterIngredient, Float>> any = getIngredientMap().entrySet().stream().filter(entry -> entry.getKey().equals(finalIngredient)).findAny();
                return any.map(Map.Entry::getValue).orElse(-1f);
            } else
                return -1;
        }
        
        @Override
        public int getRepairAmount(ItemStack stack) {
            BetterIngredient ingredient = null;
            for (BetterIngredient ingredient1 : getIngredients()) {
                if (ingredient1.isIncluded(stack)) {
                    ingredient = ingredient1;
                    break;
                }
            }
            if (ingredient != null) {
                float multiplier;
                BetterIngredient finalIngredient = ingredient;
                Optional<Map.Entry<BetterIngredient, Float>> any = getIngredientMap().entrySet().stream().filter(entry -> entry.getKey().equals(finalIngredient)).findAny();
                multiplier = any.map(Map.Entry::getValue).orElse(Float.MAX_VALUE / 2);
                return MathHelper.ceil(fullAmount * multiplier);
            } else
                return -1;
        }
    }
    
}


