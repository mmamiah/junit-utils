package lu.mms.common.quality.assets.fixture;

@Fixture
public class DefaultFixture {

    private BookService bookOne;

    private BookService redBook;

    private BookService bigBook;

    public BookService getBookOne() {
        return bookOne;
    }

    public BookService getRedBook() {
        return redBook;
    }

    public BookService getBigBook() {
        return bigBook;
    }
}
