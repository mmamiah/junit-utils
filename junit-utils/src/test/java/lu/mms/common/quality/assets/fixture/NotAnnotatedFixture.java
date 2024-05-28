package lu.mms.common.quality.assets.fixture;

import org.mockito.internal.util.MockUtil;

import static org.mockito.Mockito.when;

class NotAnnotatedFixture {

    private BookService bookService;

    public BookService getBookService() {
        return bookService;
    }

    public void setBookService(final BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Do That in case any one call the {@link BookService#getSize()}, the answer should be twenty (20).
     */
    void givenBookSizeIsTwenty() {
        final int size = 20;
        if (MockUtil.isMock(bookService)) {
            when(bookService.getSize()).thenAnswer(arg -> size);
        } else {
            bookService.setSize(size);
        }
    }


}
