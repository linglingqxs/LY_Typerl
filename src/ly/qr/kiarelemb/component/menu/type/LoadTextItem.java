package ly.qr.kiarelemb.component.menu.type;

import ly.qr.kiarelemb.component.TextPane;
import ly.qr.kiarelemb.component.TyperTextPane;
import ly.qr.kiarelemb.component.contract.state.GroupButton;
import ly.qr.kiarelemb.component.menu.MenuItem;
import ly.qr.kiarelemb.data.Keys;
import ly.qr.kiarelemb.qq.LoadText;
import method.qr.kiarelemb.utils.QRSystemUtils;

import java.awt.event.ActionEvent;

/**
 * @author Kiarelemb QR
 * @program: 揽月跟打器
 * @description:
 * @create 2023-01-25 15:13
 **/
public class LoadTextItem extends MenuItem {
	public static final LoadTextItem LOAD_TEXT_ITEM = new LoadTextItem();

	private LoadTextItem() {
		super("载文", Keys.QUICK_KEY_MENU_TYPE_TEXT_LOAD);
		setEnabled(QRSystemUtils.IS_WINDOWS);
	}

	@Override
	protected void actionEvent(ActionEvent o) {
		if (GroupButton.groupBtn.groupLinked()) {
			String text = LoadText.getLoadText();
			if (text != null && !text.isEmpty()) {
				TextPane.TEXT_PANE.setTypeText(text);
				return;
			}
		}
		TyperTextPane.TYPER_TEXT_PANE.paste();
	}
}