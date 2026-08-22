import './BackgroundOrbit.css'

function BackgroundOrbit({
                             className = '',
                         }) {
    return (
        <>
            <div
                className={
                    `background-orbit background-orbit--left ${className}`
                }
                aria-hidden="true"
            >
                <span className="background-orbit-cross" />
            </div>

            <div
                className={
                    `background-orbit background-orbit--right ${className}`
                }
                aria-hidden="true"
            >
                <span className="background-orbit-cross" />
            </div>
        </>
    )
}

export default BackgroundOrbit