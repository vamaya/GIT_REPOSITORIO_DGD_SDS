package co.gov.banrep.iconecta.office.documento.entity;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFSDT;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public class Validaciones {
	
	public boolean validartags(XWPFDocument docx) {

		boolean valid = false;

		Iterator<IBodyElement> elemas = docx.getBodyElements().stream().filter(t -> t instanceof XWPFTable)
				.collect(Collectors.toList()).iterator();

		while (elemas.hasNext()) {
			IBodyElement uelemas = elemas.next();

			List<XWPFTableRow> row = ((XWPFTable) uelemas).getRows();
			Iterator<XWPFTableRow> iterrow = row.iterator();
			while (iterrow.hasNext()) {

				XWPFTableRow urow = iterrow.next();
				int tamanoCeldas = urow.getCtRow().sizeOfTcArray();

				for (int i = 0; i < tamanoCeldas; i++) {
					XWPFTableCell cel = urow.getCell(i);
					if (!cel.equals(null)) {

						for (IBodyElement icel : cel.getBodyElements()) {
							if (icel instanceof XWPFSDT) {
								String text = ((XWPFSDT) icel).getTag();
								if (text.contains("00_NOMBRE_FIRMANTE")) {
									valid = true;
								}
							}
						}
					}
				}
			}
		}
		return valid;
	}
}
