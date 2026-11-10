package br.com.lucas.ms1_user.application.port.out;

import br.com.lucas.ms1_user.domain.model.CsvUserRow;

import java.util.List;

public interface CsvUserReaderPort {

    List<CsvUserRow> parse(String content);

}
