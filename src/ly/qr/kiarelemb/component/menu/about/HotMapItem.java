package ly.qr.kiarelemb.component.menu.about;

import ly.qr.kiarelemb.component.menu.MenuItem;
import ly.qr.kiarelemb.type.KeyTypedRecordWindow;

import java.awt.event.ActionEvent;

/**
 * @author Kiarelemb
 * @projectName LYTyper
 * @className HotMapItem
 * @description TODO
 * @create 2024/4/13 20:37
 */
public class HotMapItem extends MenuItem {

    public static final HotMapItem HOT_MAP_ITEM = new HotMapItem();

    private HotMapItem() {
        super("跟打热力图", null);
    }

    /**
     * 已自动添加监听器，可直接重写
     *
     * @param o
     */
    @Override
    protected void actionEvent(ActionEvent o) {
        KeyTypedRecordWindow window = new KeyTypedRecordWindow();
        window.setVisible(true);
    }
}