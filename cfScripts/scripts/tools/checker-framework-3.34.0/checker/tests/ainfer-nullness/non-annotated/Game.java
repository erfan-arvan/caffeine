interface Game {
    void newGame();
}

class GameImpl implements Game {  // package-private
    private MoveValidator moveValidator;

    public GameImpl(MoveValidator mValidator) {
        mValidator.setGame(this);
        moveValidator = mValidator;
    }

    public GameImpl() {
    }

    @Override
    public void newGame() {
        // Implementation of starting a new game
    }
}

interface MoveValidator {
    void setGame(Game game);
}

class PlayerDependentMoveValidator implements MoveValidator {  // package-private
    private Game game;
    private MoveValidator blackMoveValidator = new SimpleMoveValidator();

    @Override
    public void setGame(Game game) {
        this.game = game;
    }

    public PlayerDependentMoveValidator() {
    }

    public PlayerDependentMoveValidator(Game game) {
        this.setGame(game);
        blackMoveValidator.setGame(game);
    }
}

class SimpleMoveValidator implements MoveValidator {  // package-private
    private Game game;

    @Override
    public void setGame(Game game) {
        this.game = game;
    }

    public SimpleMoveValidator() {
    }

    public MoveValidator createMoveValidator() {
        return new PlayerDependentMoveValidator(game);
    }
}
