package disenchanting_patch.patches;

import disenchanting.disenchanter.DisenchanterObjectEntity;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.inventory.InventoryItem;
import necesse.inventory.enchants.Enchantable;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.FieldValue;

@ModMethodPatch(target = DisenchanterObjectEntity.class, name = "processInput", arguments = {})
public class DisenchantingPatch {

    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onProcessInput(
            @Advice.This DisenchanterObjectEntity that,
            @FieldValue(value = "lastProcessTime", readOnly = false) long lastProcessTimeField,
            @FieldValue(value = "isProcessing", readOnly = false) boolean isProcessingField
    ) {
        InventoryItem gearItem = that.getInventory().getItem(0);
        InventoryItem scrollSlot = that.getInventory().getItem(1);

        if (!that.canProcessInput() || that.getOutputItem() == null || gearItem == null || scrollSlot == null) {
            return false;
        }

        that.addOutput(that.getOutputItem());

        if (gearItem.item instanceof Enchantable) {
            ((Enchantable<?>) gearItem.item).setEnchantment(gearItem, 0);
            that.getInventory().markDirty(0);
        }

        that.getInventory().removeItems(null, null, that.getInventory().getItemSlot(1), 1, "disenchant");

        isProcessingField = false;
        long currentTime = that.getWorldEntity().getWorldTime();
        lastProcessTimeField = currentTime + 1L;

        that.markProcessingDirty();

        return true;
    }
}