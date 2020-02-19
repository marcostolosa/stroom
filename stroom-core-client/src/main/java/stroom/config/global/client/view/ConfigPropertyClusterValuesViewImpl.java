package stroom.config.global.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import stroom.config.global.client.presenter.ConfigPropertyClusterValuesPresenter;
import stroom.config.global.client.presenter.ConfigPropertyClusterValuesUiHandlers;

public class ConfigPropertyClusterValuesViewImpl
    extends ViewWithUiHandlers<ConfigPropertyClusterValuesUiHandlers>
    implements ConfigPropertyClusterValuesPresenter.ConfigPropertyClusterValuesView {

    private final Widget widget;

    @Inject
    ConfigPropertyClusterValuesViewImpl(final EventBus eventBus,
                                        final Binder binder) {
        widget = binder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    public interface Binder extends UiBinder<Widget, ConfigPropertyClusterValuesViewImpl> {
    }
}
